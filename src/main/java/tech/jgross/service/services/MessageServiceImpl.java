package tech.jgross.service.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tech.jgross.service.entity.Message;


@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
  private final @NonNull Map<UUID, Message> messageRepo;

  @Override
  public void createMessage(Message message) {
    messageRepo.put(message.id(), message);
  }

  @Override
  public Message getMessage(UUID id) {
    return messageRepo.get(id);
  }

  @Override
  public void updateMessage(UUID id, Message message) {
    Message updatedMessage = messageRepo.replace(id, message);
    messageRepo.put(id, updatedMessage);
  }

  @Override
  public void deleteMessage(UUID id) {
    messageRepo.remove(id);

  }

  @Override
  public List<Message> getMessages() {
    return messageRepo.values().stream().collect(Collectors.toList());
  }
}