package uk.httpclient.test;
import java.io.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.file.*;
import io.vertx.core.streams.Pipe;


public class HttpLoopVerticle extends AbstractVerticle {
    private static final int PERIOD = 3000;
    private static final String STORAGE_PATH = "/tmp/out-http";
    private final HttpClientOptions opts;
    private HttpClient client;

    private final String host;
    private final int port;
    private final String path;

    public HttpLoopVerticle(String host, int port, String path, boolean keepalive) {
        super();

        opts = new HttpClientOptions();
        opts.setKeepAlive(keepalive);

        this.host = host;
        this.port = port;
        this.path = path;
    }

    public void start(Promise<Void> promise) {
        this.client = vertx.createHttpClient(opts);
        this.doTheThing(0);
        promise.complete();
    }

    public void doTheThing(long id) {
        Future<Integer> fut = fetch();
        fut.onComplete(ar -> {
            if (ar.succeeded()) {
                int len = ar.result();
                System.out.println("Got " + len + " bytes from response.");
            } else {
                System.out.println(ar.cause());
            }

            vertx.setTimer(PERIOD, this::doTheThing);
        });
    }

    public Future<Integer> fetch() {
        final OpenOptions crops = new OpenOptions().setRead(false).setWrite(true).setCreate(true).setTruncateExisting(true);
        FileSystem fileSystem = vertx.fileSystem();

        Promise<Integer> promise = Promise.promise();

        client.request(HttpMethod.GET, port, host, path, req_ar -> {
            if (!req_ar.succeeded()) {
                promise.fail(req_ar.cause());
                return;
            }

            HttpClientRequest req = req_ar.result();
            req.send().onComplete(resp_ar -> {
                if (!resp_ar.succeeded()) {
                    promise.fail(resp_ar.cause());
                    return;
                }

                HttpClientResponse resp = resp_ar.result();
                AsyncFile file = fileSystem.openBlocking(STORAGE_PATH, crops);
                Pipe<Buffer> pipe = resp.pipe();
                // pipe.endOnComplete(true);
                pipe.to(file, opres -> {
                    if (opres.failed()) {
                        promise.fail(opres.cause());
                    } else {
                        promise.complete((int) (new File(STORAGE_PATH)).length());
                    }
                });
            });
        });
        return promise.future();
    }
}
