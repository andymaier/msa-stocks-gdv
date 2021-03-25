package com.predic8.stock.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.predic8.stock.model.ObjectError;
import com.predic8.stock.model.Stock;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ShopListener {
	private final ObjectMapper mapper;
	private final Map<String, Stock> repo;
	private final NullAwareBeanUtilsBean beanUtils;
	private final KafkaTemplate<String, Operation> kafka;

	public ShopListener(ObjectMapper mapper, Map<String, Stock> repo, NullAwareBeanUtilsBean beanUtils, KafkaTemplate<String, Operation> kafka) {
		this.mapper = mapper;
		this.repo = repo;
		this.beanUtils = beanUtils;
		this.kafka = kafka;
	}

	@KafkaListener(topics = "shop")
	public void listen(Operation op) throws Exception {
		System.out.println("op = " + op);

		if(!op.getBo().equals("article")) return;

		Stock stock = mapper.treeToValue(op.getObject(), Stock.class);

		switch(op.getAction()) {
			case "upsert":
					if(!repo.containsKey(stock.getUuid())) {
						repo.put(stock.getUuid(), stock);
					}
					break;
			case "delete":
					repo.remove(stock.getUuid());
					break;
			default:
				ObjectError oenew = new ObjectError("Error", mapper.writeValueAsString(op));
				Operation opnew = new Operation("obejct_error", "error_catalogue", mapper.valueToTree(oenew));
				kafka.send("shop_error", opnew);
		}

	}
}