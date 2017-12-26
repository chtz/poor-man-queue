package ch.furthermore.pmq;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <pre>
 * $ curl -X POST -H "Content-Type: application/json" -d "[]" http://localhost:4040/queues
 * accf184f-7106-44de-b63a-6e76d85fc63d
 * 
 * $ curl -X PUT -H "Content-Type: application/json" -d '["Halli","Hallo"]' http://localhost:4040/queues/accf184f-7106-44de-b63a-6e76d85fc63d
 * accf184f-7106-44de-b63a-6e76d85fc63d
 * 
 * $ curl http://localhost:4040/queues/accf184f-7106-44de-b63a-6e76d85fc63d
 * ["Kartoffel"]
 * </pre>
 */
@Controller
public class QueueController {
	@Autowired
	private QueueDAO queueDAO;

	@RequestMapping(path="/queues", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	@ResponseBody
	public String createQueue(@RequestBody List<String> data) {
		try {
			return queueDAO.save(null, data);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(path="/queues/{queueId}", method=RequestMethod.PUT, consumes="application/json", produces="application/json")
	@ResponseBody
	public String addToQueue(@RequestBody List<String> data, @PathVariable("queueId") String queueId) {
		try {
			LinkedList<String> queue = queueDAO.load(queueId);
			queue.addAll(data);
			return queueDAO.save(queueId, queue);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(path="/queues/{queueId}", method=RequestMethod.GET, produces="application/json") //FIXME idempotent GET
	@ResponseBody
	public List<String> flushQueue(@PathVariable("queueId") String queueId) {
		try {
			LinkedList<String> queue = queueDAO.load(queueId);
			queueDAO.save(queueId, new LinkedList<String>());
			return queue;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
