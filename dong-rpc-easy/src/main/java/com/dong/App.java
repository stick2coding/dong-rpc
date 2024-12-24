package com.dong;

import com.dong.dongrpc.server.DongHttpServer;
import com.dong.dongrpc.server.VertxDongHttpServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        DongHttpServer dongHttp = new VertxDongHttpServer();
        dongHttp.doStart(8080);

    }
}
