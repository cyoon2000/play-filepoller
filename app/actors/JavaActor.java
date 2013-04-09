package actors;

import actors.FileUtilities;
import models.FileEntry;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

//import akka.util.Duration;
//import akka.japi.Function;
//import java.util.concurrent.Callable;
//import static akka.dispatch.Futures.future;
//import static akka.dispatch.Futures.sequence;
//import static java.util.concurrent.TimeUnit.SECONDS;

import akka.dispatch.*;
import akka.util.Timeout;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.ExecutionContextExecutorService;

import akka.dispatch.Future;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;

import anorm.NotAssigned;
import anorm.Id;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.Long;
import java.lang.String;
import java.util.zip.Checksum;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.IOUtils;


public class JavaActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() {
        log.info("javaActor is running");
    }

    public void onReceive(Object message) {
        //if (message instanceof Work) {
        if (message instanceof String) {


            File f = new File("/www/a/data/test/filepoller/child/test55.tif");
            //addToDb(f, Long.toString(getChecksum(f)), "/www/a/data/test/filepoller");

        } else {
            log.error("Got a message I don't understand.");
            unhandled(message);
        }
    }


//    public void checkFileSystem() {
//        //Some source generating a sequence of Future<Integer>:s
//        Iterable<Future<Integer>> listOfFutureInts = source;
//
//        // now we have a Future[Iterable[Integer]]
//        Future<Iterable<Integer>> futureListOfInts = sequence(listOfFutureInts, system.dispatcher());
//
//        // Find the sum of the odd numbers
//        Future<Long> futureSum = futureListOfInts.map(new Mapper<Iterable<Integer>, Long>() {
//            public Long apply(Iterable<Integer> ints) {
//                long sum = 0;
//                for (Integer i : ints)
//                    sum += i;
//                return sum;
//            }
//        });
//
//        long result = Await.result(futureSum, Duration.create(1, SECONDS));
//    }

    public long getChecksum(File f) {
      try {
//        long crc = 0;
//        FileInputStream  fis = new FileInputStream(f);
//        CheckedInputStream cis = new CheckedInputStream(fis, new CRC32());
//        byte[] buffer = new byte[100];
//        while (cis.read(buffer) >= 0) {
//            crc = cis.getChecksum().getValue();
//        }
//        return crc;

        Checksum checksum = new CRC32();
        byte [] bytes = IOUtils.toByteArray(new BufferedInputStream(new FileInputStream(f)));
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();

      } catch(Exception e) {
          log.error(e.getMessage());
          return 0;
      }
    }


//    public void addToDb(File f, String signature, String rootDir) {
//        log.info("......this file is ready. adding the record in DB : " + f + ", signature = " + signature);
//        FileEntry.create(new FileEntry(f.getAbsolutePath(), signature, f.rootDir));
//
//    }
//
//    public void removeFromDB(FileEntry fileEntry) {
//        log.info("......deleting the record in DB : " + fileEntry);
//        //if (!FileEntry.delete(fileEntry.id)) log.error("Error deleting the record " + fileEntry);
//        if (!FileEntry.deleteJava(fileEntry)) log.error("Error deleting the record " + fileEntry);
//    }

    public static class Work {
        public String msg;
        public Work(String msg) {
        	            super();
        	            this.msg = msg;
        }
    }

}