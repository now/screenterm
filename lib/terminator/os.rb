# -*- coding: utf-8 -*-

module Terminator::OS
  def self.name
    initialize
    @name
  end

  def self.arch
    initialize
    @arch
  end

  def self.target
    @target ||= '%s_%s' % [arch, name]
  end

  def self.cygwin?
    @name == 'Cygwin'
  end

  def self.darwin?
    @name == 'Darwin'
  end

  def self.linux?
    @name == 'Linux'
  end

  def self.jvmify(path)
    return path unless cygwin?
    command = "cygpath --windows '%s'" % path
    $stderr.printf "running %s\n", command
    output = `#{command}`
    $stderr.printf "result: %s\nstatus: %p\n", output, $?
    raise '%s failed' % command unless $?.success
    output.chomp
  end

  def self.pathify(path)
    path.uniq.map{ |e| jvmify(e) }.join(cygwin? ? ';' : File::PATH_SEPARATOR)
  end

private

  def self.initialize
    @name, @arch =
      case Config::CONFIG['target_os']
      when 'cygwin'
        %w[Cygwin i386]
      when 'mswin32'
        %w[Windows i386]
      when /^darwin/
        %w[Darwin universal]
      else
        [ `uname`.chomp,
          `uname -m`.chomp.sub(/i[456]86/, 'i386').sub(/x86_64/, 'amd64') ]
      end
  end
end
