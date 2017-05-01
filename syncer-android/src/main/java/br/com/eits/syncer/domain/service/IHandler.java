package br.com.eits.syncer.domain.service;

import java.util.Observable;

/**
 * Created by eits on 26/04/17.
 */
public interface IHandler<T>
{
    public void handle( T result );
}
