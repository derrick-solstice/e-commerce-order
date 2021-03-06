package com.solstice.ecommerceorder.service;

import com.solstice.ecommerceorder.data.AccountFeignProxy;
import com.solstice.ecommerceorder.data.LineRepository;
import com.solstice.ecommerceorder.data.OrderRepository;
import com.solstice.ecommerceorder.domain.Line;
import com.solstice.ecommerceorder.domain.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.support.NullValue;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class OrderManagementServiceUnitTests {

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private LineManagementServices lineManagementServices;

    @MockBean
    private AccountFeignProxy accountFeignProxy;

    private OrderManagementService orderManagementService;

    @Before
    public void setup(){
        orderManagementService = new OrderManagementService(orderRepository, lineManagementServices, accountFeignProxy);
    }

    @Test
    public void getOneOrder_HappyPath(){
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getMockOrder()));
        when(lineManagementServices.getAllLinesForOrder(12345L)).thenReturn(getMockLineList());
        when(lineManagementServices.getAllShipmentsForLines(any())).thenReturn("[ TESTING ARRAY ]");
        when(accountFeignProxy.getAccount(anyLong())).thenReturn("{TestAccountData}");
        when(accountFeignProxy.getAddress(anyLong(), anyLong())).thenReturn("{TestShippingAddress}");

        Order foundOrder = orderManagementService.getOneOrder(12345L);
        assertThat(foundOrder.getAccountId(), is(1L));
        assertThat(foundOrder.getAccount(), is("{TestAccountData}"));
        assertThat(foundOrder.getShippingAddressId(), is(1L));
        assertThat(foundOrder.getShippingAddress(), is("{TestShippingAddress}"));
        assertThat(foundOrder.getOrderDate().toString(), is("2018-08-15"));
        assertThat(foundOrder.getLineItems().size(), is(6));
        assertThat(foundOrder.getShipments(), is(notNullValue()));

    }

    @Test
    public void getAllOrders_HappyPath(){

        when(orderRepository.findAll()).thenReturn(getMockOrderList());

        List<Order> foundOrders = orderManagementService.getAllOrders();
        assertThat(foundOrders.size(), is(4));
        assertThat(foundOrders.get(3).getOrderNumber(), is(12345L));
    }

    @Test
    public void createOrder_HappyPath(){
        when(orderRepository.save(any(Order.class))).thenReturn(getMockOrder());
        when(accountFeignProxy.getAccount(1L)).thenReturn("ACCOUNT FOUND");

        Order savedOrder = orderManagementService.createOrder(getOrderToSave());

        assertThat(savedOrder.getOrderNumber(), is(12345L));
        assertThat(savedOrder.getOrderDate().toString(), is("2018-08-15"));


    }

    @Test
    public void deleteOrder_HappyPath(){

        when(orderRepository.findById(12345L)).thenReturn(Optional.ofNullable(new Order()));
        orderManagementService.deleteOrder(12345L);

        verify(orderRepository, times(1)).findById(12345L);
        verify(orderRepository, times(1)).deleteById(12345L);
    }

    @Test
    public void updateOrder_HappyPath(){
        Order updateOrder = new Order(5L, 15L);
        when(orderRepository.findById(12345L)).thenReturn(Optional.ofNullable(getMockOrder()));
        when(accountFeignProxy.getAccount(5L)).thenReturn("ACCOUNT EXISTS");

        Order updatedOrder = orderManagementService.updateOrder(12345L, updateOrder);
        assertThat(updatedOrder.getOrderNumber(), is(12345L));
        assertThat(updatedOrder.getOrderDate().toString(), is("2018-08-15"));
        assertThat(updatedOrder.getTotalPrice(), is(BigDecimal.ZERO));
        assertThat(updatedOrder.getAccountId(), is(5L));
        assertThat(updatedOrder.getShippingAddressId(), is(15L));
    }

    //Convenience Methods
    public static Order getMockOrder(){
        Order mockOrder = new Order();
        mockOrder.setOrderDate( LocalDate.of(2018,8,15));
        mockOrder.setOrderNumber(12345L);
        mockOrder.setTotalPrice(BigDecimal.ZERO);
        mockOrder.setLineItems(new ArrayList<>());
        mockOrder.setAccountId(1L);
        mockOrder.setShippingAddressId(1L);
        return mockOrder;
    }

    public static Order getOrderToSave(){
        return new Order( 1L, 1L);
    }

    public static List<Order> getMockOrderList(){
        return new ArrayList<Order>(){{
            add(getMockOrder());
            add(getMockOrder());
            add(getMockOrder());
            add(getMockOrder());
        }};
    }

    //Convenience Methods
    private static Line getMockLine(Long lineId){
        Line mockLine = new Line(20, new BigDecimal("150.00"), 5L ,12345L );
        mockLine.setLineItemId(lineId);
        mockLine.setShipmentId(67890L);
        return mockLine;
    }

    private static List<Line> getMockLineList(){
        return new ArrayList<Line>(){{
            add(getMockLine(15L));
            add(getMockLine(16L));
            add(getMockLine(17L));
            add(getMockLine(18L));
            add(getMockLine(19L));
            add(getMockLine(20L));
        }};
    }

}
