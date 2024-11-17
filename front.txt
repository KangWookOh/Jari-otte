<template>
  <div class="my-page-container">
    <div class="my-page">
      <h1>마이 페이지</h1>
      <div class="button-container">
        <button @click="activeSection = 'user'" :class="{ active: activeSection === 'user' }">회원 정보</button>
        <button @click="activeSection = 'coupons'" :class="{ active: activeSection === 'coupons' }">쿠폰</button>
        <button @click="activeSection = 'reservations'" :class="{ active: activeSection === 'reservations' }">예매 내역</button>
        <button @click="activeSection = 'payments'" :class="{ active: activeSection === 'payments' }">결제 내역</button>
      </div>

      <div v-if="activeSection === 'user'" class="section">
        <h2>회원 정보</h2>
        <form @submit.prevent="updateUser">
          <div class="form-group">
            <label for="nickname">닉네임</label>
            <input id="nickname" v-model="user.nickname" required>
          </div>
          <div class="form-group">
            <label for="password">비밀번호</label>
            <input id="password" v-model="user.password" type="password" required>
          </div>
          <button type="submit" class="submit-button">정보 수정</button>
        </form>
      </div>

      <div v-if="activeSection === 'coupons'" class="section">
        <h2>사용 가능한 쿠폰</h2>
        <div class="coupon-list">
          <div v-for="coupon in coupons" :key="coupon.couponCode" class="coupon-item">
            <div class="coupon-main">
              <div class="coupon-header">
                <span class="coupon-name">{{ coupon.couponName }}</span>
                <span class="coupon-code">{{ coupon.couponCode }}</span>
              </div>
              <div class="coupon-details">
                <div class="discount-info">
                  {{ coupon.discountType === 'PERCENTAGE'
                  ? `${coupon.discount}% 할인`
                  : `${coupon.price.toLocaleString()}원 할인` }}
                </div>
                <div class="coupon-type">{{ getCouponTypeText(coupon.couponType) }}</div>
              </div>
            </div>
            <div class="coupon-status" :class="{ 'inactive': !coupon.isActive }">
              {{ coupon.isActive ? '사용 가능' : '사용 불가' }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeSection === 'reservations'" class="section">
        <h2>예매 내역</h2>
        <div v-if="reservations.length > 0" class="list-container">
          <div v-for="reservation in reservations" :key="reservation.seatId" class="list-item">
            <div class="item-header">
              <span class="item-title">{{ reservation.concertTitle }}</span>
              <span class="item-date">{{ formatDate(reservation.createdAt) }}</span>
            </div>
            <div class="item-details">
              <span>좌석: {{ reservation.seatId }}</span>
              <span class="item-price">{{ reservation.price.toLocaleString() }}원</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-message">예매 내역이 없습니다.</div>
        <div v-if="reservations.length > 0" class="pagination">
          <button @click="changePage('reservations', -1)" :disabled="reservationPage === 1">이전</button>
          <span>{{ reservationPage }} / {{ reservationTotalPages }}</span>
          <button @click="changePage('reservations', 1)" :disabled="reservationPage === reservationTotalPages">다음</button>
        </div>
      </div>

      <div v-if="activeSection === 'payments'" class="section">
        <h2>결제 내역</h2>
        <div v-if="payments.length > 0" class="list-container">
          <div v-for="payment in payments" :key="payment.concertId" class="list-item">
            <div class="item-header">
              <span class="item-title">{{ payment.concertTitle }}</span>
              <span class="payment-status" :class="payment.payStatus.toLowerCase()">
                {{ getPaymentStatusText(payment.payStatus) }}
              </span>
            </div>
            <div class="item-details">
              <span>{{ payment.payInfo }}</span>
              <span class="item-price">{{ payment.amount.toLocaleString() }}원</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-message">결제 내역이 없습니다.</div>
        <div v-if="payments.length > 0" class="pagination">
          <button @click="changePage('payments', -1)" :disabled="paymentPage === 1">이전</button>
          <span>{{ paymentPage }} / {{ paymentTotalPages }}</span>
          <button @click="changePage('payments', 1)" :disabled="paymentPage === paymentTotalPages">다음</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const activeSection = ref('user');
const user = ref({ nickname: '', password: '' });
const coupons = ref([]);
const reservations = ref([]);
const payments = ref([]);
const reservationPage = ref(1);
const reservationTotalPages = ref(1);
const paymentPage = ref(1);
const paymentTotalPages = ref(1);

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

const getCouponTypeText = (type) => {
  const types = {
    'LIMIT': '한정 쿠폰',
    'UNLIMITED': '무제한 쿠폰'
  };
  return types[type] || type;
};

const getPaymentStatusText = (status) => {
  const statuses = {
    'PENDING': '결제 대기',
    'COMPLETED': '결제 완료',
    'CANCELLED': '결제 취소'
  };
  return statuses[status] || status;
};

const fetchUserInfo = async () => {
  try {
    const { data } = await api.get('/users/my');
    user.value = data.data;
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error);
    handleApiError(error);
  }
};

const updateUser = async () => {
  try {
    const { data } = await api.patch('/users/my', user.value);
    alert('회원 정보가 수정되었습니다.');
    user.value = data.data;
  } catch (error) {
    console.error('사용자 정보 업데이트 실패:', error);
    if (error.response) {
      alert(`회원 정보 수정에 실패했습니다. 서버 응답: ${error.response.data?.message || error.response.status}`);
    } else if (error.request) {
      alert('서버에서 응답이 없습니다. 네트워크 연결을 확인해주세요.');
    } else {
      alert(`회원 정보 수정 중 오류가 발생했습니다: ${error.message}`);
    }
    handleApiError(error);
  }
};

const fetchCoupons = async () => {
  try {
    const { data } = await api.get('/coupons/my-coupon');
    coupons.value = data.data;
  } catch (error) {
    console.error('쿠폰 정보 가져오기 실패:', error);
    handleApiError(error);
  }
};

const fetchReservations = async () => {
  try {
    const { data } = await api.get('/reservations', {
      params: { page: reservationPage.value - 1, size: 4 },
    });
    const reservationsWithTitles = await Promise.all(data.data.content.map(async (reservation) => {
      const concertResponse = await api.get(`/concerts/${reservation.concertId}`);
      return {
        ...reservation,
        concertTitle: concertResponse.data.data.title
      };
    }));
    reservations.value = reservationsWithTitles;
    reservationTotalPages.value = data.data.totalPages;
  } catch (error) {
    console.error('예약 정보 가져오기 실패:', error);
    handleApiError(error);
  }
};

const fetchPayments = async () => {
  try {
    const { data } = await api.get('/payments', {
      params: { page: paymentPage.value - 1, size: 5 },
    });
    const paymentsWithTitles = await Promise.all(data.data.content.map(async (payment) => {
      const concertResponse = await api.get(`/concerts/${payment.concertId}`);
      return {
        ...payment,
        concertTitle: concertResponse.data.data.title
      };
    }));
    payments.value = paymentsWithTitles;
    paymentTotalPages.value = data.data.totalPages;
  } catch (error) {
    console.error('결제 정보 가져오기 실패:', error);
    handleApiError(error);
  }
};

const changePage = (section, delta) => {
  if (section === 'reservations') {
    reservationPage.value += delta;
    fetchReservations();
  } else if (section === 'payments') {
    paymentPage.value += delta;
    fetchPayments();
  }
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const handleApiError = async (error) => {
  if (error.response?.status === 401) {
    router.push('/login');
  }
  return Promise.reject(error);
};

watch(activeSection, (newSection) => {
  if (newSection === 'user') fetchUserInfo();
  else if (newSection === 'coupons') fetchCoupons();
  else if (newSection === 'reservations') fetchReservations();
  else if (newSection === 'payments') fetchPayments();
});

onMounted(() => {
  const token = localStorage.getItem('token');
  if (!token) {
    router.push('/login');
  } else {
    fetchUserInfo();
  }
});
</script>

<style scoped>
.my-page-container {
  min-height: 100vh;
  background-color: #D9A66C;
  padding: 2rem;
}

.my-page {
  max-width: 800px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  font-size: 1.8rem;
  color: #333;
  text-align: center;
  margin-bottom: 2rem;
}

h2 {
  font-size: 1.4rem;
  color: #333;
  margin-bottom: 1.5rem;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.button-container button {
  padding: 0.75rem 1.5rem;
  background-color: transparent;
  border: 1px solid #D9A66C;
  border-radius: 4px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.button-container button.active {
  background-color: #D9A66C;
  color: white;
}

.button-container button:hover {
  background-color: #D9A66C;
  color: white;
}

.section {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.submit-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #D9A66C;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.submit-button:hover {
  background-color: #c08b50;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.coupon-item {
  border: 1px solid #D9A66C;
  border-radius: 8px;
  padding: 1rem;
  background-color: white;
}

.coupon-main {
  margin-bottom: 1rem;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.coupon-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

.coupon-code {
  background-color: #D9A66C;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.9rem;
}

.coupon-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.discount-info {
  font-weight: bold;
  color: #D9A66C;
}

.coupon-type {
  color: #666;
  font-size: 0.9rem;
}

.coupon-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #28a745;
  font-size: 0.9rem;
}

.coupon-status.inactive {
  color: #dc3545;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.list-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.item-title {
  font-weight: bold;
  color: #333;
}

.item-date {
  color: #666;
  font-size: 0.9rem;
}

.item-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
}

.item-price {
  font-weight: bold;
  color: #D9A66C;
}

.payment-status {
  font-size: 0.9rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.payment-status.completed {
  color: #28a745;
}

.payment-status.pending {
  color: #ffc107;
}

.payment-status.cancelled {
  color: #dc3545;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.pagination button {
  padding: 0.5rem 1rem;
  background-color: #D9A66C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.pagination button:disabled {
  background-color: #ddd;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  background-color: #c08b50;
}

.empty-message {
  text-align: center;
  color: #666;
  padding: 20px;
}

@media (max-width: 768px) {
  .my-page-container {
    padding: 1rem;
  }

  .button-container {
    flex-wrap: wrap;
  }

  .button-container button {
    flex: 1 1 calc(50% - 0.5rem);
  }
}
</style>
