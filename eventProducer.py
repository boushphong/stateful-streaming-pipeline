from confluent_kafka import Producer
import random
import time
import sys


class Randomizer:
    id = range(1,30)
    amount = range(1, 100)
    payment_type = ["debit", "credit"]


def streaming_payment(kafka_producer, kafka_topic):
    try:
        while True:
            data = {'id': random.choice(Randomizer.id),
                    'amount': random.choice(Randomizer.amount),
                    'payment_type': random.choice(Randomizer.payment_type)
                    }
            kafka_producer.produce(kafka_topic, key=str(data['id']),
                                   value=f"{data.get('id')},{data.get('amount')},{data.get('payment_type')}")
            print(f"Inserted user {data['id']} with {data['amount']} on {data['payment_type']}")
            time.sleep(random.uniform(0, 1))
    except KeyboardInterrupt:
        producer.flush()
        print("Ending job ...")


if __name__ == "__main__":
    topic = sys.argv[1]
    producer = Producer({'bootstrap.servers': 'localhost:9092'})
    streaming_payment(producer, topic)
