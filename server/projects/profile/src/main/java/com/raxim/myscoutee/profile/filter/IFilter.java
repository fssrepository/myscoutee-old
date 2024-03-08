package com.raxim.myscoutee.profile.filter;

public interface IFilter<T, U> {
    T filter(T t, U u);
}
