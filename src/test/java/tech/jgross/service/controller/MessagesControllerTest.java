package tech.jgross.service.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import tech.jgross.service.entity.Message;
import tech.jgross.service.services.MessageService;


@SpringBootTest
@AutoConfigureMockMvc
public class MessagesControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private MessageService messageService;

	private OngoingStubbing<List<Message>> thenReturn;

	@Test
	public void getMessages() throws Exception {
		HashMap<UUID, Message> messages = new HashMap<UUID, Message>();

		UUID id = UUID.randomUUID();
		Message message = new Message(id, "sender", "data");
		messages.put(id, message);

		thenReturn = when(messageService.getMessages())
				.thenReturn((List<Message>) messages.values());

		mvc.perform(
				MockMvcRequestBuilders
						.get("/messages")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("[]"));
	}
}
