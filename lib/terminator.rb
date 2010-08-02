# -*- coding: utf-8 -*-

require 'net/telnet'
require 'pathname'
require 'uri'

class Terminator
  autoload :Client, 'terminator/client'
  autoload :Directory, 'terminator/directory'
  autoload :Java, 'terminator/java'
  autoload :OS, 'terminator/os'
  autoload :Server, 'terminator/server'
  autoload :Terminfo, 'terminator/terminfo'

  Root = Pathname.new(__FILE__).realpath.dirname.dirname

  def self.start
    # TODO: report_exceptions is broken
#    report_exceptions 'Terminator', 'now@bitwi.se' do
      terminator = new
      terminator.start unless terminator.connect
#    end
  end

  def initialize
    @server = Terminator::Server.new
  end

  def connect
    @server.connect
  end

  def start
    Terminator::Terminfo.install
    select_shell
    @server.start
  end

  def select_shell
    return unless ENV.fetch('SHELL', "").empty?
    ENV['SHELL'] = ['/bin/bash', '/bin/sh'].find{ |s| File.executable? s }
  end
end
