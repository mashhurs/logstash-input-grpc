# encoding: utf-8
require "logstash/inputs/base"
require "logstash/namespace"
require "logstash-input-grpc_jars"

class LogStash::Inputs::Grpc < LogStash::Inputs::Base

  #java_import "io.netty.handler.codec.http.HttpUtil"
  #java_import 'org.logstash.plugins.inputs.http.util.SslSimpleBuilder'

  config_name "grpc"

  default :codec, "plain"

  config :host, :validate => :string, :default => "0.0.0.0"

  # The TCP port to bind to
  config :port, :validate => :number, :default => 8080

  public
  def register
    @logger.info("Registering gRPC input plugin...")
  end # def register

  def run(queue)
    @queue = queue
    #@logger.info("Starting gRPC input listener", :address => "#{@host}:#{@port}")
    #@http_server.run()
    @logger.info("Running gRPC input plugin...")
  end

  def stop
    @logger.info("Stopping gRPC input plugin...")
    #@http_server.close() rescue nil
  end

  def close
    @logger.info("Closing gRPC input plugin...")
    #@http_server.close() rescue nil
  end

end # class LogStash::Inputs::Grpc
