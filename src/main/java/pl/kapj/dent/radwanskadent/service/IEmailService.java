package pl.kapj.dent.radwanskadent.service;

public interface IEmailService {

    void sendMail(String receiver, String subject, String content);

}
