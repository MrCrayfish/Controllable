package com.mrcrayfish.controllable.registry;

import lombok.Getter;

import java.security.PrivilegedActionException;

public class ButtonNotFoundException extends IllegalArgumentException {
    @Getter
    private String action;

    /**
     * Constructs an <code>IllegalArgumentException</code> with no
     * detail message.
     */
    public ButtonNotFoundException(String action) {
        super();
        this.action = action;
    }

    /**
     * Constructs an <code>IllegalArgumentException</code> with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    public ButtonNotFoundException(String action, String s) {
        super(s);
        this.action = action;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method).  (A <tt>null</tt> value
     *                is permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.5
     */
    public ButtonNotFoundException(String action, String message, Throwable cause) {
        super(message, cause);
        this.action = action;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.5
     */
    public ButtonNotFoundException(String action, Throwable cause) {
        super(cause);
        this.action = action;
    }
}
