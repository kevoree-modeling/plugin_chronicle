package org.kevoree.modeling.plugin;


import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.KMessage;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ChroniclePlugin implements KContentDeliveryDriver {

    private boolean _isConnected = false;

    private ChronicleMap<String, String> raw;

    private long _maxEntries = 1000000;
    private File _storage = null;

    public ChroniclePlugin(long maxEntries, File storage) {
        this._maxEntries = maxEntries;
        this._storage = storage;
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        if (!_isConnected) {
            long[] defKey = new long[]{KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG};
            if (this._storage != null) {
                try {
                    raw = ChronicleMapBuilder.of(String.class, String.class)
                            .averageKey(KContentKey.toString(defKey, 0))
                            .averageValueSize(100)
                            .entries(_maxEntries)
                            .createPersistedTo(this._storage);
                } catch (IOException e) {
                    callback.on(e);
                }
            } else {
                raw = ChronicleMapBuilder.of(String.class, String.class)
                        .averageKey(KContentKey.toString(defKey, 0))
                        .averageValueSize(100)
                        .entries(_maxEntries)
                        .create();
            }
            _isConnected = true;
            if (callback != null) {
                callback.on(null);
            }
        } else {
            if (callback != null) {
                callback.on(null);
            }
        }
    }

    private String _connectedError = "PLEASE CONNECT YOUR DATABASE FIRST";

    @Override
    public void atomicGetIncrement(long[] key, KCallback<Short> cb) {
        String result = raw.get(KContentKey.toString(key, 0));
        short nextV;
        short previousV;
        if (result != null) {
            try {
                previousV = Short.parseShort(result);
            } catch (Exception e) {
                e.printStackTrace();
                previousV = Short.MIN_VALUE;
            }
        } else {
            previousV = 0;
        }
        if (previousV == Short.MAX_VALUE) {
            nextV = Short.MIN_VALUE;
        } else {
            nextV = (short) (previousV + 1);
        }
        raw.put(KContentKey.toString(key, 0), nextV + "");
        cb.on(previousV);
    }

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = keys.length / 3;
        String[] result = new String[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            result[i] = raw.get(KContentKey.toString(keys, i));
        }
        if (callback != null) {
            callback.on(result);
        }
    }

    @Override
    public void put(long[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = p_keys.length / 3;
        for (int i = 0; i < nbKeys; i++) {
            if (p_values[i] == null) {
                raw.put(KContentKey.toString(p_keys, i), "");
            } else {
                raw.put(KContentKey.toString(p_keys, i), p_values[i]);
            }
        }
        if (additionalInterceptors != null) {
            additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                @Override
                public void on(int key, KContentUpdateListener value) {
                    if (value != null && key != excludeListener) {
                        value.onKeysUpdate(p_keys);
                    }
                }
            });
        }
        if (p_callback != null) {
            p_callback.on(null);
        }
    }

    @Override
    public void remove(long[] p_keys, KCallback<Throwable> error) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        try {
            int nbKeys = p_keys.length / 3;
            for (int i = 0; i < nbKeys; i++) {
                raw.remove(KContentKey.toString(p_keys, i));
            }
            if (error != null) {
                error.on(null);
            }
        } catch (Exception e) {
            if (error != null) {
                error.on(e);
            }
        }
    }

    @Override
    public void close(KCallback<Throwable> error) {
        raw.close();
        _isConnected = false;
        if (error != null) {
            error.on(null);
        }
    }

    private ArrayIntMap<KContentUpdateListener> additionalInterceptors = null;

    /**
     * @ignore ts
     */
    private Random random = new Random();

    /**
     * @native ts
     * return Math.random();
     */
    private int randomInterceptorID() {
        return random.nextInt();
    }

    @Override
    public synchronized int addUpdateListener(KContentUpdateListener p_interceptor) {
        if (additionalInterceptors == null) {
            additionalInterceptors = new ArrayIntMap<KContentUpdateListener>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        }
        int newID = randomInterceptorID();
        additionalInterceptors.put(newID, p_interceptor);
        return newID;
    }

    @Override
    public synchronized void removeUpdateListener(int id) {
        if (additionalInterceptors != null) {
            additionalInterceptors.remove(id);
        }
    }

    @Override
    public String[] peers() {
        return new String[0];
    }

    @Override
    public void sendToPeer(String peer, KMessage message, KCallback<KMessage> callback) {
        if (callback != null) {
            callback.on(null);
        }
    }

}
