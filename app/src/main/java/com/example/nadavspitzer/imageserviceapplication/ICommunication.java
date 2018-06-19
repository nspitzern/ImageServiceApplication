package com.example.nadavspitzer.imageserviceapplication;

public interface ICommunication {

    /*******
     * the function starts the communication with the tcp connection
     */
    void Start();

    /*********
     * the function stops the communication with the tcp connection
     */
    void Stop();

    /*********
     * the function sends a picture as an array of bytes to the server
     * @param pic an array of bytes the represents a picture.
     */
    void Send(byte[] pic);
}
