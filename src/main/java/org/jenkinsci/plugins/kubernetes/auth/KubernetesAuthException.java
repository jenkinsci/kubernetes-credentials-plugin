package org.jenkinsci.plugins.kubernetes.auth;

public class KubernetesAuthException extends Exception {
    public KubernetesAuthException() { super(); }
    public KubernetesAuthException(String message) { super(message); }
    public KubernetesAuthException(String message, Throwable cause) { super(message, cause); }
    public KubernetesAuthException(Throwable cause) { super(cause); }
}
