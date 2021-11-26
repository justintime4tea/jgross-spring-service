package tech.jgross.service.services;

import java.util.List;
import java.util.UUID;

import tech.jgross.service.entity.Message;


public interface MessageService {
  public abstract void createMessage(Message message);

  public abstract Message getMessage(UUID id);

  public abstract void updateMessage(UUID id, Message message);

  public abstract void deleteMessage(UUID id);

  public abstract List<Message> getMessages();
}