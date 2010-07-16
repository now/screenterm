module Terminator::Terminfo
  Definition = Terminator::Root + '.generated' + 'terminfo' + 'terminator'

  def self.install
    users_terminfo = ENV['TERMINFO']
    unless users_terminfo
      raise 'You need to set your HOME environment variable' unless home = ENV['HOME']
      users_terminfo = File.join(home, '.terminfo')
    end
    path = Pathname(users_terminfo)
    ['t', '74'].each do |sub|
      install_in path + sub
    end
  end

  def self.install_in(path)
    target = path + 'terminator'
    return if same? Definition, target
    path.mkpath
    FileUtils.cp Definition, target
  end

  def self.same?(a, b)
    a.open('rb'){ |f| f.read } == b.open('rb'){ |f| f.read }
  rescue
    false
  end
end
