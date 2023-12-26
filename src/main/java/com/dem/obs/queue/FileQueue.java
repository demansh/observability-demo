package com.dem.obs.queue;

import com.dem.obs.entities.Message;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class FileQueue {

    private static final String QUEUE_FILE_NAME = "queue.txt";
    private static final Path QUEUE_FILE_PATH = Paths.get(QUEUE_FILE_NAME);
    private static final int MAX_LOCK_ATTEMPTS = 10;
    private static final int MAX_LOCK_AWAIT = 1000;
    private static final FileChannel LOCK_FILE;
    private static FileLock LOCK;

    static {
        try {
            LOCK_FILE = new RandomAccessFile("LOCKFILE", "rw").getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(Message message) throws IOException, TimeoutException {
        tryLock();
        try (FileOutputStream fos = new FileOutputStream(QUEUE_FILE_NAME, true)) {
            String str = serialize(message);
            fos.write(str.getBytes(StandardCharsets.UTF_8));
        } finally {
            unlock();
        }
    }

    public Message pop() throws IOException, TimeoutException {
        tryLock();

        File queueFile = new File(QUEUE_FILE_NAME);
        File tmpFile = new File(tempFileName());

        try (FileInputStream fis = new FileInputStream(queueFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                return null;
            } else {
                FileOutputStream fos = new FileOutputStream(tmpFile);
                reader.lines().forEach(line -> {
                    try {
                        fos.write(String.format("%s\n", line).getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        try {
                            fos.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        throw new RuntimeException(e);
                    }
                });
                if (tmpFile.renameTo(queueFile)) {
                    return deserialize(firstLine);
                } else {
                    return null;
                }
            }
        } finally {
            unlock();
        }
    }

    public long size() throws IOException, TimeoutException {
        tryLock();

        try (Stream<String> stream = Files.lines(QUEUE_FILE_PATH, StandardCharsets.UTF_8)) {
            return stream.count();
        } finally {
            unlock();
        }
    }

    private Message deserialize(String rawString) {
        return new Message(rawString.split(":")[0], rawString.split(":")[1]);
    }

    private String serialize(Message message) {
        return String.format("%s:%s\n", message.id(), message.text());
    }

    private String tempFileName() {
        return String.format("%s.txt", UUID.randomUUID());
    }

    private void tryLock() throws IOException, TimeoutException {
        tryLock(0, 10L);
    }

    private void tryLock(int attempt, long prev) throws IOException, TimeoutException {
        try {
            LOCK = LOCK_FILE.tryLock();
        } catch (IOException | OverlappingFileLockException e) {
            if (attempt >= MAX_LOCK_ATTEMPTS || prev > MAX_LOCK_AWAIT) {
                throw new TimeoutException("Queue timeout");
            }
            long duration = 0L;
            try {
                duration = prev + new Random().nextLong(10) * attempt;
                Thread.sleep(duration);
            } catch (InterruptedException interruptedException) {
                //ignore
            }
            tryLock(attempt + 1, duration);
        }
    }

    private void unlock() throws IOException {
        LOCK.release();
    }
}
