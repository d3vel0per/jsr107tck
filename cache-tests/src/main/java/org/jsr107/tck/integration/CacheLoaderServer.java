/**
 *  Copyright (c) 2011-2016 Terracotta, Inc.
 *  Copyright (c) 2011-2016 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */
package org.jsr107.tck.integration;

import org.jsr107.tck.support.OperationHandler;
import org.jsr107.tck.support.Server;

import javax.cache.integration.CacheLoader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;

/**
 * A {@link Server} that handles {@link CacheLoader} requests from a
 * {@link CacheLoaderClient} and delegates them to an underlying {@link CacheLoader}.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @author Brian Oliver
 */
public class CacheLoaderServer<K, V> extends Server {
  /**
   * The underlying {@link CacheLoader} that will be used to
   * load entries requested by the {@link CacheLoaderClient}s.
   */
  private volatile CacheLoader<K, V> cacheLoader;

  /**
   * Constructs an {@link CacheLoaderServer} (without a {@link CacheLoader} to
   * which client requests will be delegated).
   *
   * @param port the port on which to accept {@link CacheLoaderClient} requests
   */
  public CacheLoaderServer(int port) {
    this(port, null);
  }

  /**
   * Constructs an CacheLoaderServer.
   *
   * @param port        the port on which to accept {@link CacheLoaderClient} requests
   * @param cacheLoader (optional) the {@link CacheLoader} that will be used to handle
   *                    client requests
   */
  public CacheLoaderServer(int port, CacheLoader<K, V> cacheLoader) {
    super(port);

    // establish the client-server operation handlers
    addOperationHandler(new LoadOperationHandler());
    addOperationHandler(new LoadAllOperationHandler());

    this.cacheLoader = cacheLoader;
  }

  /**
   * Set the {@link CacheLoader} the {@link CacheLoaderServer} should use
   * from now on.
   *
   * @param cacheLoader the {@link CacheLoader}
   */
  public void setCacheLoader(CacheLoader<K, V> cacheLoader) {
    this.cacheLoader = cacheLoader;
  }

  /**
   * The {@link OperationHandler} for a {@link CacheLoader#loadAll(Iterable)}} operation.
   */
  public class LoadAllOperationHandler implements OperationHandler {
    @Override
    public String getType() {
      return "loadAll";
    }

    @Override
    public void onProcess(ObjectInputStream ois,
                          ObjectOutputStream oos) throws IOException, ClassNotFoundException {

      if (cacheLoader == null) {
        throw new NullPointerException("The CacheLoader for the CacheLoaderServer has not be set");
      } else {
        HashSet<K> keys = new HashSet<K>();

        K key = (K) ois.readObject();
        while (key != null) {
          keys.add(key);

          key = (K) ois.readObject();
        }

        Map<K, V> map = null;
        try {
          map = cacheLoader.loadAll(keys);
        } catch (Exception e) {
          oos.writeObject(e);
        }

        if (map != null) {
          for (Map.Entry<K, V> entry : map.entrySet()) {
            oos.writeObject(entry.getKey());
            oos.writeObject(entry.getValue());
          }
          oos.writeObject(null);
        }
      }
    }
  }

  /**
   * The {@link OperationHandler} for a {@link CacheLoader#load(Object)} operation.
   */
  public class LoadOperationHandler implements OperationHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
      return "load";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProcess(ObjectInputStream ois,
                          ObjectOutputStream oos) throws IOException, ClassNotFoundException {
      if (cacheLoader == null) {
        throw new NullPointerException("The CacheLoader for the CacheLoaderServer has not be set");
      } else {
        K key = (K) ois.readObject();

        V value = null;
        try {
          value = cacheLoader.load(key);
          oos.writeObject(value);
        } catch (Exception e) {
          oos.writeObject(e);
        }
      }
    }
  }
}
