package com.raxim.myscoutee.common.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.raxim.myscoutee.profile.data.dto.rest.PageItemDTO;

public class ControllerUtil {

    @FunctionalInterface
    public static interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public static interface CheckedBiFunction<T, U, R> {
        R apply(T t, U u) throws Exception;
    }

    @FunctionalInterface
    public static interface CheckedTriFunction<T, U, V, R> {
        R apply(T t, U u, V v) throws Exception;
    }

    public static <T, E extends PageItemDTO> ResponseEntity<E> handle(Function<T, Optional<E>> function, T p1,
            HttpStatus status) {
        try {
            Optional<E> result = function.apply(p1);
            if (result.isPresent()) {
                return new ResponseEntity<>(result.get(), status);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // use logger
            return ResponseEntity.badRequest().build();
        }
    }

    public static <T, U, E extends PageItemDTO> ResponseEntity<E> handle(
            CheckedBiFunction<T, U, Optional<E>> function,
            T p1,
            U p2, HttpStatus status) {
        try {
            Optional<E> result = function.apply(p1, p2);
            if (result.isPresent()) {
                return new ResponseEntity<>(result.get(), status);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // use logger
            return ResponseEntity.badRequest().build();
        }
    }

    public static <T, U, V, E extends PageItemDTO> ResponseEntity<E> handle(
            CheckedTriFunction<T, U, V, Optional<E>> function,
            T p1,
            U p2, V p3, HttpStatus status) {
        try {
            Optional<E> result = function.apply(p1, p2, p3);
            if (result.isPresent()) {
                return new ResponseEntity<>(result.get(), status);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // use logger
            return ResponseEntity.badRequest().build();
        }
    }
}
