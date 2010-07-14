# -*- coding: utf-8 -*-

class Terminator::Client
  def initialize(connection)
    @connection = connection
    @secret = Pathname(@connection.to_str + '.secret')
    @secret.open('wb'){ |f| f.chmod(0600) }
  end

  def try_to_send(command)
    try_to_send! command
    true
  rescue => ex
    false
  end
  
  def send(command)
    try_to_send! command
    true
  rescue => ex
    $stderr.puts(ex)
    false
  end

  def try_to_send!(command)
    port = /:(\d+)$/.match(@connection.open('rb'){ |f| f.read })[1] or
      raise 'no port given in server connection file: %s' % @connection
    secret = @secret.open('rb'){ |f| f.read }
    telnet = Net::Telnet.new('Port' => port.to_i,
                             'Telnetmode' => false)
    telnet.puts secret
    telnet.puts command
    output = telnet.readlines
    telnet.close
    response = output.shift.chomp
    raise response unless response == 'Authentication OK'
    # TODO: Um, what is this doing?
    print output.join("")
  end
end
