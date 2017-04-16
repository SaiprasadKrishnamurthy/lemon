CREATE OR REPLACE FUNCTION clients_activity_watcher() RETURNS trigger AS
$$
BEGIN
  	SELECT content FROM http_get('http://localhost:8765/cache-clear?name=DeviceTypeCount');
  	SELECT content FROM http_get('http://localhost:8765/cache-clear?name=RoutersMemoryAvgUtilizationTimeSeries');
  	SELECT content FROM http_get('http://localhost:8765/cache-clear?name=SwitchesMemoryAvgUtilizationTimeSeries');
RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER clients_activity_notif
AFTER INSERT OR UPDATE OR DELETE ON clients_activity EXECUTE PROCEDURE clients_activity_watcher();


insert into clients_activity(client_ip, client_type, client_status, memory_utilization, cpu_utilization, timestamp) values('1.1.1.2', 'router', 'active', 83, 71, 1492314452221);


select client_type, count(client_type) from clients_activity  where client_status='active' group by client_type;

select * from clients_activity;

insert into clients_activity(client_ip, client_type, client_status, memory_utilization, cpu_utilization, timestamp) values('1.1.1.3', 'router', 'active', 85, 71, 1492314457221);

CREATE EXTENSION http;

SELECT urlencode('my special string''s & things?');

SELECT content FROM http_get('http://localhost:8765/cache-clear?name=DeviceTypeCount');

CREATE OR REPLACE FUNCTION clients_activity_watcher() RETURNS trigger AS
$$
BEGIN
  	PERFORM content FROM http_get('http://localhost:8765/cache-clear?name=DeviceTypeCount');
  	PERFORM content FROM http_get('http://localhost:8765/cache-clear?name=DeviceUsageTrend');
RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER clients_activity_notif
AFTER INSERT OR UPDATE OR DELETE ON clients_activity EXECUTE PROCEDURE clients_activity_watcher();




