# -*- coding: utf-8 -*-

class Terminator::Java
  autoload :ClassPath, 'terminator/java/classpath'

  # Command-line tools are probably best off with this trivial interface.
  # It certainly removes boilerplate from your start-up script.
  def self.runCommandLineTool(className)
    invoker = new(className, className)
    invoker.initiate_startup_notification = false
    invoker.invoke
  end

  def initialize(name, class_name)
    @dock_name = name
    self.icons = name
    @class_name = class_name
    @log = nil
    @initiate_startup_notification = true

    @extra_java_arguments = []

    @class_path = ClassPath.new

    add_pathnames_property 'org.jessies.libraryDirectories',
      Dir.glob('%s/.generated/*_%s/lib' %
                [Terminator::Root,
                 Terminator::OS.name])
    add_pathname_property 'org.jessies.binaryDirectory', binaries
  end

  attr_accessor :log, :initiate_startup_notification, :class_name

  def add_property(name, value)
    @extra_java_arguments << '-D%s=%s' % [name, value]
  end

  def add_pathname_property(name, path)
    add_property name, Terminator::OS.jvmify(path)
  end

  def add_pathnames_property(name, paths)
    add_property name, Terminator::OS.pathify(paths)
  end

  def icons=(name)
    @dock_icon = '%s/lib/%s.icns' % [Terminator::Root, name]
    @frame_icon = '%s/lib/%s-32.png' % [Terminator::Root, name.downcase]
  end

  def invoke
    launch
#    report_exceptions(@dock_name) { launch() }
  end

  def binaries
    path = '%s/.generated/{%s,*_%s}/bin' %
      [Terminator::Root, Terminator::OS.target, Terminator::OS.name]
    @binaries ||= Dir.glob(path).first or
      raise 'failed to find any support binary directories: %s' % path
  end

  def launch()
    args = [(Terminator::OS.cygwin? or ENV.include? 'USE_JAVA_LAUNCHER') ? 
      binaries+'java-launcher' :
      'java'
    ]

    @extra_java_arguments << @class_path.to_s

    # Since we're often started from the command line or from other programs,
    # set up startup notification ourselves if it looks like we should.
    ENV['DESKTOP_STARTUP_ID'] =
      `#{binaries+'gnome-startup'} start #{@frame_icon} Starting #{@dock_name}`.chomp if
      @initiate_startup_notification and
      ENV.include? 'DISPLAY' and
      not ENV.include? 'DESKTOP_STARTUP_ID' and
      Terminator::OS.linux?

    # Pass any GNOME startup notification id through as a system property.
    # That way it isn't accidentally inherited by the JVM's children.  We test
    # for the empty string because GDK doesn't unset the variable, it sets it
    # to the empty string, presumably for portability reasons.
    id = ENV.delete('DESKTOP_STARTUP_ID')
    add_property 'gnome.DESKTOP_STARTUP_ID', id if id and not id.empty?

    begin
      @log.open('wb'){ }
      add_pathname_property 'e.util.Log.filename', @log
    rescue SystemCallError => ex
      # Inability to create the log file is not fatal.
    end if @log and not ENV.include? "DEBUGGING_#{@dock_name.upcase}"

    add_property 'e.util.Log.applicationName', @dock_name

    args << heap_size_arg

    add_os_specific_args args

    add_pathname_property 'org.jessies.frameIcon', @frame_icon
    add_pathname_property 'org.jessies.projectRoot', Terminator::Root

    # Work around Sun bug 6274341.
    add_property 'java.awt.Window.locationByPlatform', 'true'

    # Work around the Metal LAF's ugliness. Not needed in Java 6?
    add_property "swing.boldMetal", 'false'

    args.concat @extra_java_arguments

    args << @class_name

    args.concat ARGV

    return if system(*args)

    # We're only interested in debugging unexpected exiting here.  When Java
    # gets a SIGINT (signal number 2), it exits "normally" with status 130,
    # so termsig() will be nil here.  Java mimics the behavior of bash in
    # propagating SIGINT like this.
    ['INT', 'TERM'].each do |signal|
      status = Signal.list[signal] + 0x80
      exit status if $?.exitstatus == status or $?.termsig == Signal.list[signal]
    end

    # It’s not an error for a command-line program to exit with a non-zero
    # exit status.
    exit 1 if not @initiate_startup_notification and $?.exitstatus == 1

    raise <<EOE
Java failed with #{$?.inspect}.

Please send the contents of #{@log}.

An idea of what you were doing when Java exited might be useful.

Command line was #{args.join(' ')}.
EOE
  end

private

  def heap_size_arg
    # Portably determining how much RAM we have is a game.
    # sysctl hw.usermem on Mac OS
    # cat /proc/meminfo on Linux and Cygwin
    # sysconf(_SC_PHYS_PAGES) * sysconf(_SC_PAGESIZE) on Solaris?
    '-Xmx%s' % (Terminator::OS.cygwin? ? '100m' : '1g')
  end
  
  def add_os_specific_args(args)
    return unless Terminator::OS.darwin?
    args << '-Xdock:name=%s' % @dock_name
    args << '-Xdock:icon=%s' % @dock_icon
    add_property 'apple.laf.useScreenMenuBar', 'true'
  end
end

if __FILE__ == $0
  Terminator::Java.new('Launcher', 'e/util/Launcher').invoke
end
