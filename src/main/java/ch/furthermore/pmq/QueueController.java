package ch.furthermore.pmq;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <pre>
 * $ curl -X POST -H "Content-Type: application/json" -d "[]" https://pmq.furthermore.ch/queues
 * fb1b23a9-00ef-4032-a68e-9159adeb65e8
 * 
 * $ curl -X PUT -H "Content-Type: application/json" -d '["Halli","Hallo"]' https://pmq.furthermore.ch/queues/fb1b23a9-00ef-4032-a68e-9159adeb65e8
 * fb1b23a9-00ef-4032-a68e-9159adeb65e8
 * 
 * $ curl https://pmq.furthermore.ch/queues/fb1b23a9-00ef-4032-a68e-9159adeb65e8
 * ["Halli","Hallo"]
 * </pre>
 * 
 * <pre>
 * $ curl -X POST -H "Content-Type: application/json" -d "[]" http://localhost:4040/queues
 * cef6c757-9035-44c2-85bb-858be5fb11b8
 * 
 * $ curl -X POST -H "Content-Type: application/json" -d '{"item":"hallo"}' http://localhost:4040/queues/cef6c757-9035-44c2-85bb-858be5fb11b8
 * {"count":"1"}
 * 
 * $ curl http://localhost:4040/queues/cef6c757-9035-44c2-85bb-858be5fb11b8?poll=long
 * ["hallo"]
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
	public String addItemsToQueue(@RequestBody List<String> data, @PathVariable("queueId") String queueId) {
		try {
			LinkedList<String> queue = queueDAO.load(queueId);
			queue.addAll(data);
			return queueDAO.save(queueId, queue);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(path="/queues/{queueId}", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	@ResponseBody
	@Deprecated //for PMW compatibility: maps as in- and out params
	public Map<String,String> addItemToQueue(@RequestBody Map<String,String> request, @PathVariable("queueId") String queueId) { //FIXME atomic
		try {
			String item = request.get("item");
			
			LinkedList<String> queue = queueDAO.load(queueId);
			queue.add(item);
			
			queueDAO.save(queueId, queue);
			
			Map<String,String> response = new HashMap<String,String>();
			response.put("count", Integer.toString(queue.size()));
			return response;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(path="/queues/{queueId}", method=RequestMethod.GET, produces="application/json") //FIXME idempotent GET
	@ResponseBody
	public List<String> getAndRemoveAllItemsFromQueue(@PathVariable("queueId") String queueId, @RequestParam(name="poll", defaultValue="short") String poll) {
		return getAndRemoveAllItemsFromQueue(queueId, "long".equals(poll)
				? System.currentTimeMillis() + 10 * 1000
				: System.currentTimeMillis() - 1
				);
	}
	
	public List<String> getAndRemoveAllItemsFromQueue(String queueId, long timeout) { //FIXME atomic
		try {
			LinkedList<String> queue = queueDAO.load(queueId);
			if (queue.isEmpty() && System.currentTimeMillis() < timeout) {
				Thread.sleep(1000); //FIXME don't waste resources (threads) - use async servlet & timer (or async proto like websockets or ...)
				return getAndRemoveAllItemsFromQueue(queueId, timeout);
			}
			else {
				queueDAO.save(queueId, new LinkedList<String>());
				return queue;
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
