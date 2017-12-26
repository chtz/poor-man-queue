package ch.furthermore.pmq;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QueueDAO {
	private final static ObjectMapper om = new ObjectMapper();
	
	@Value(value="${storagePath:storage}")
	private String storagePath;
	private File storage;
	
	@PostConstruct
	public void init() {
		storage = new File(storagePath);
		storage.mkdirs();
	}

	@SuppressWarnings("unchecked")
	public synchronized LinkedList<String> load(String id) throws IOException { //FIXME lock file, not DAO ;-)
		File queueFile = new File(storage, UUID.fromString(id).toString() + ".json");
		return om.readValue(queueFile, LinkedList.class);
	}
	
	public synchronized String save(String id, List<String> data) throws IOException {
		id = id == null ? UUID.randomUUID().toString() : UUID.fromString(id).toString();
		File queueFile = new File(storage, id + ".json");
		om.writeValue(queueFile, data);
		return id;
	}
}
