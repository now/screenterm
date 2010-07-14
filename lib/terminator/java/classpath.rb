# -*- coding: utf-8 -*-

class Terminator::Java::ClassPath
  def initialize
    @paths = [
      '%s/.generated/classes.jar' % Terminator::Root,
      '%s/.generated/classes' % Terminator::Root,
      '%s/.generated/classes' % Terminator::SalmaHayek
    ].concat(Dir.glob('%s/lib/jars/*.jar' % Terminator::SalmaHayek)).
      concat(Dir.glob('%s/lib/jars/*.jar' % Terminator::Root))
  end

  # TODO: Perhaps take java in constructor and have a method like
  # java.propertize(name, value)?
  def to_s
    '-Djava.class.path=%s' % Terminator::OS.pathify(@paths)
  end
end

=begin
    if target_os() != "Darwin"
      # It's sometimes useful to have classes from "tools.jar".
      # It doesn't exist on Mac OS X but the classes are on the boot class path anyway.
      # On Win32 it's likely that users don't have a JDK on their path, in
      # which case they probably aren't interested in running anything that
      # wouldn't work without "tools.jar", so we cope with not having found
      # a JDK.
      require "#{@salma_hayek}/bin/find-jdk-root.rb"
      jdk_root = find_jdk_root()
      if jdk_root != nil
        tools_jar = "#{jdk_root}/lib/tools.jar"
        if File.exist?(tools_jar)
          @class_path << tools_jar
        end
      end
    end

    @class_path.concat(jars.to_a())
  end
=end
