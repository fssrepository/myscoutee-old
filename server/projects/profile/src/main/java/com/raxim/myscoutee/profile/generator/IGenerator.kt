package com.raxim.myscoutee.profile.generator

interface IGenerator<T, U> {
    fun generate(flags: T): List<Set<U>>
}