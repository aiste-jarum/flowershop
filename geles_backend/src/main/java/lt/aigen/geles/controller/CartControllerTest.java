package lt.aigen.geles.controller;

import org.junit.Test;
import lt.aigen.geles.models.dto.CartDTO;
import lt.aigen.geles.models.dto.FlowerInCartDTO;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;





public class CartControllerTest {


    Long id;
    CartController auth = new CartController(); 
    CartDTO cartDTO = new CartDTO();
    @Test//testuojama ar nedirbame su neegzistuojanciu krepseliu
    void cartExistTest() {
       
        id = 10000000L; 
        
        ResponseEntity response = auth.UpdateCart(cartDTO, id);
        assertThat(response, hasStatus(404));
    }

    @Test //Testuojama ar neprileidzia neautorizuotu asmenu prie krepselio 
    void authentication(){

        id = 10000000L;
        ResponseEntity response = auth.UpdateCart(cartDTO, id);
        assertThat(response, hasStatus(401));
        
    }

    @Test //patikrina ar ciklas praeina ir duomenys yra issiunciami
    void sendOK(){
        ResponseEntity response = auth.UpdateCart(cartDTO, id);
        assrtThat(response, hasStatus(200));
    }


}


