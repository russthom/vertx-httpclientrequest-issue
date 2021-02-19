package uk.httpclient.test;

import io.vertx.core.*;

public class Main
{

    public static void main(String[] args)
    {
        String host = "127.0.0.1";
        int port = 8080;
        String path = "/testfile";
        boolean keepalive = true;
        boolean worker = true;
        int wps = 20;

        System.out.println("http://" + host + ":" + port + path + ", keepalives " + (keepalive ? "on" : "off"));

        VertxOptions vertopts = new VertxOptions();
        vertopts.setWorkerPoolSize(wps);

        DeploymentOptions depopts = new DeploymentOptions();
        depopts.setWorker(worker);

        Vertx vertx = Vertx.vertx(vertopts);

        System.out.println("Deploying HttpLoopVerticle.");
        vertx.deployVerticle(new HttpLoopVerticle(host, port, path, keepalive), depopts);
    }
}
