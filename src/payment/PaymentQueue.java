package payment;

public class PaymentQueue {

	// 결제 요청을 하나의 공용 Queue에서 관리하기 위해 싱글톤으로 구현
	// PaymentService가 enqueue, PaymentWorker가 dequeue할 때 동일한 Queue를 공유해야 하기 때문
	private static final PaymentQueue INSTANCE = new PaymentQueue();

    private PaymentQueue() {}

    public static PaymentQueue getInstance() {
        return INSTANCE;
    }

    public void enqueue(int paymentId) {
        // Queue 처리 구현 예정
    }
}