require "fileutils.rb"
require "pathname.rb"
require "uri"

class Terminator
  Home = Pathname.new(__FILE__).realpath.dirname.dirname
  SalmaHayek = (Home.dirname + 'salma-hayek').realpath

  ['invoke-java.rb', 'show-alert.rb', 'target-os.rb'].each do |dependency|
    require SalmaHayek + 'bin' + dependency
  end
   
  def self.start
    report_exceptions 'Terminator', 'now@bitwi.se' do
      new.start
    end
  end

  def start
        # Allow different Terminators to have different options (and logging).
        dot_directory = ENV["TERMINATOR_DOT_DIRECTORY"]
        if dot_directory == nil
            home = ENV["HOME"]
            dot_directory = "#{home}/.terminator"
        end
        # Make sure we have a directory for Terminator's private files and another for the logs.
        log_directory = File.join(dot_directory, "logs")
        make_directory(dot_directory, true)
        make_directory(log_directory, false)
        options_pathname = File.join(dot_directory, "options")
        
        server_port_file_stem = File.join(dot_directory, "terminator-server-port")
        # Colons have a special meaning on Windows file systems.
        display = ENV["DISPLAY"]
        display_suffix = ""
        if display != nil
            display_suffix = display.gsub(/[:\/]/, "_")
        end
        server_port_file_name = "#{server_port_file_stem}#{display_suffix}"
        tryInAppServer(server_port_file_name)
        
        # terminfo installation...
        
        # We deliberately omit the intermediate directory.
        compiled_terminfo = "#{Home}/.generated/terminfo/terminator"
        
        # Make sure our terminfo is available system-wide, if possible.
        
        # tic(1) might put terminfo into /etc/terminfo/ but the FHS (and hence Debian) says:
        # "/etc: Host-specific system configuration"
        # /usr/share/terminfo/ is where the FHS says terminfo should be (though Debian puts architecture-specific terminfo in /lib/terminfo/).
        # Mac OS doesn't have /etc/terminfo/.
        # FIXME: if we ever offer a Solaris package, Blastwave's ncurses library looks in /opt/csw/share/terminfo/.
        # FIXME: since our ".deb" file will be the right place to install system-wide on Debian-based systems, and Mac OS users won't be able to write outside their home directory, we should probably stop trying to install system-wide from here when we're building ".deb" packages.
        system_wide_terminfo = "/usr/share/terminfo/"
        if File.writable?(system_wide_terminfo)
            # We know what kind of system we're on, so we can put the file in the right place and not look clueless.
            prefix = (target_os() == "Darwin") ? "74" : "t"
            install_terminfo_in(compiled_terminfo, File.join(system_wide_terminfo, prefix))
        end
        
        # Make sure our terminfo is available for the current user.
        
        # Use the user-defined terminfo directory, or their ~/.terminfo otherwise.
        # We write here even if we can write to #{system_wide_terminfo} in case they can access their home directory from other machines where they can't install system-wide.
        user_terminfo_root = ENV['TERMINFO']
        if user_terminfo_root == nil
            user_terminfo_root = "#{ENV['HOME']}/.terminfo"
        end
        # Mac OS won't look where every other OS looks; presumably a %x instead of a %c somewhere.
        # I think they fixed it at some point, but it seems broken in 10.4 where /usr/share/terminfo/ only contains %x directories.
        # We always write both possibilities under ~/.terminfo or $TERMINFO for the benefit of people who use Mac OS and other Unixes.
        install_terminfo_in(compiled_terminfo, File.join(user_terminfo_root, "t"))
        install_terminfo_in(compiled_terminfo, File.join(user_terminfo_root, "74"))
        
        selectShellIfNoDefault()
        
        invoker = Java.new("Terminator", "terminator/Terminator")
        invoker.log_filename = "#{log_directory}/terminator-#{$$}.log"
        invoker.add_pathname_property("org.jessies.terminator.serverPortFileName", server_port_file_name)
        invoker.launch()
    end
    
    def make_directory(pathname, make_safe)
        directory_exists = false
        directory_safe = ! make_safe
        begin
            if test(?d, pathname) == false
                FileUtils.rm_f(pathname)
                Dir.mkdir(pathname)
            end
            directory_exists = true
            if make_safe
                if File.stat(pathname).mode != 0700
                    FileUtils.chmod(0700, pathname)
                end
            end
            directory_safe = true
        rescue SystemCallError => ex
            if directory_exists == false
                show_alert("Terminator had trouble starting.", "Terminator was unable to create the directory \"#{pathname}\".")
                exit(1)
            elsif directory_safe == false
                show_alert("Terminator had trouble starting.", "Terminator was unable to change the permissions on \"#{pathname}\" to make it safe. Others may be able to read your logs or cause new terminals to open on your display.")
            end
        end
    end
    
    def tryInAppServer(serverPortPathname)
        # InAppClient's constructor stops anyone else from reading the .secret file.
        client = InAppClient.new(serverPortPathname)
        if ENV["DEBUGGING_TERMINATOR"]
            return
        end
        # We should start a new Terminator if the client (this script) is newer than the server (the currently-running Terminator).
        # Checking just the modification time of just the script is only an approximation, but it's close enough for users who'll install all the files at once anyway.
        if FileUtils.uptodate?(serverPortPathname, [__FILE__]) == false
            return
        end
        # The existing application's current directory might not be the same.
        workingDirectory = Dir.getwd()
        arguments = [ "parseCommandLine", "--working-directory", workingDirectory ]
        arguments.concat(ARGV)
        encodedArguments = arguments.map() {
            |argument|
            URI.encode(argument)
        }
        encodedArgumentString = encodedArguments.join(" ")
        # See if there's already a Terminator running that can open us a new window.
        # This lets us emulate the Mac OS behavior when clicking on the Dock icon on Linux (from gnome-panel or whatever).
        # In particular, using this trick we can open a new window quicker than xterm(1), from the user's point of view.
        
        # If you use Terminator on two machines, you don't want new terminals to appear arbitrarily on one of them.
        # (If you call the evergreen script on two machines, by contrast, you are likely to want the files to be opened on just one machine.)
        # Terminator's InAppServer only listens on the loopback address.
        # If the host running Terminator isn't the same as localhost, then we are unlikely to be able to make a connection and should fall back to the unoptimized code path.
        client.overrideHost("localhost")
        if client.trySendCommand(encodedArgumentString) == false
            return
        end
        
        # Job done, so finish any GNOME startup notification and exit.
        # We need to do this here because the existing Terminator won't have the right DESKTOP_STARTUP_ID.
        desktop_startup_id = ENV['DESKTOP_STARTUP_ID']
        if desktop_startup_id != nil
            system("#{SalmaHayek}/.generated/#{target_directory()}/bin/gnome-startup", "stop", desktop_startup_id)
        end
        # parseCommandLine now blocks until the requested window is closed.
        exit(0)
    end
    
    def have_same_content(file1, file2)
        # Even in 1.9, Ruby's string equality is a memcmp of the underlying byte array.
        return IO.read(file1) == IO.read(file2)
    end
    
    def install_terminfo_in(original_file, directory)
        terminfo_file = File.join(directory, "terminator")
        if File.exist?(terminfo_file) && have_same_content(terminfo_file, original_file)
            return
        end
        if ENV["DEBUGGING_TERMINATOR"]
            puts("installing #{terminfo_file}")
        end
        FileUtils.mkdir_p(directory)
        # We used to use a timestamp comparison and :preserve => true.
        # This caused a number of different symptoms on Cygwin.
        # The underlying problem always seemed to be related.
        # Cygwin hadn't been given a mapping from the Windows owner/group to a Unix owner/group.
        FileUtils.cp(original_file, terminfo_file)
    end
    
    def selectShellIfNoDefault()
        if ENV.include?("SHELL")
            return
        end
        # cygwin.bat runs bash --login -i, which behaves differently from bash started as /bin/sh.
        # In particular, tab completion is disabled in this "posix" mode if "vi" line editing is requested.
        # Everyone(?) else probably has SHELL=/bin/bash already in the environment.
        candidateShells = []
        candidateShells << "/bin/bash"
        # There couldn't be a Unix without /bin/sh, though POSIX doesn't mandate it.
        # POSIX explicitly recommends rewriting your shell scripts at install-time by calling getconf(1) and sed(1).
        # It doesn't mandate locations for them either.
        # See http://www.opengroup.org/onlinepubs/009695399/utilities/sh.html.
        candidateShells << "/bin/sh"
        candidateShells.each() {
            |candidateShell|
            # We can't do this test in the Java on Cygwin.
            if File.executable?(candidateShell)
                ENV["SHELL"] = candidateShell
                return
            end
        }
    end
end
