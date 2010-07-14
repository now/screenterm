# -*- coding: utf-8 -*-

class Terminator::Server
  def initialize
    @home = Terminator::Directory.new
    @connection = @home.path + ('server-port' + ENV.fetch('DISPLAY', "").gsub(/[:\/]/, '_'))
  end

  def connect
    return false unless FileUtils.uptodate? @connection, [__FILE__]
    stop_gnome_startup if actually_connect
    true
  end

  def actually_connect
    Terminator::Client.new(@connection).try_to_send [
      'parseCommandLine', *ARGV
    ].map{ |a| URI.encode(a) }.join(' ')
  end

  def stop_gnome_startup
    return unless id = ENV['DESKTOP_STARTUP_ID']
    system Terminator::SalmaHayek + '.generated' + target_directory + 'bin' + 'gnome-startup',
           'stop', id
  end

  def start
    invoker = Terminator::Java.new('Terminator', 'terminator/Terminator')
    invoker.log = @home.logs + ('terminator-%s.log' % $$)
    invoker.add_pathname_property 'org.jessies.terminator.serverPortFileName', @connection
    invoker.launch
  end
end
