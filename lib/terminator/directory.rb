# -*- coding: utf-8 -*-

class Terminator::Directory
  def initialize
    home = ENV['TERMINATOR_HOME']
    unless home
      raise 'HOME environment variable not set' unless home = ENV['HOME']
      home = File.join(home, '.terminator')
    end
    @path = Pathname(home)
    @logs = @path + 'logs'
    prepare
  end

  attr_reader :path, :logs

private

  def prepare
    create_directory @path
    set_directory_mode @path, 0700
    create_directory @logs
  end

  def create_directory(path)
    path.mkpath
  rescue SystemCallError => e
    raise e, 'unable to create directory: %s: %s' % [path, e]
  end

  def set_directory_mode(path, mode)
    path.chmod mode unless path.stat.mode == mode
  rescue SystemCallError => e
    raise e,
      'unable to set security mode %s on directory: %s: %s' % 
        [mode.to_s(8), path, e]
  end
end
