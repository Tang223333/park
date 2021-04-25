package com.example.park.util;

import java.util.UUID;

public class UUIDWorker {

    public static String getId(){
        return (UUID.randomUUID().toString().replace("-",""));
    }
}
