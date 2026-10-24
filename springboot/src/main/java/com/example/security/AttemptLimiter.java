package com.example.security;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Component
public class AttemptLimiter {
    private final Map<String,long[]> windows=new HashMap<>();
    public synchronized void check(String key,int limit,long duration) {
        long now=System.currentTimeMillis();
        windows.entrySet().removeIf(e->e.getValue()[0]<=now);
        long[] window=windows.get(key);
        if(window==null){ if(windows.size()>=10000) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"请求过于频繁"); window=new long[]{now+duration,0};windows.put(key,window); }
        if(++window[1]>limit) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"尝试次数过多，请稍后再试");
    }
}
