package psam.portfolio.sunder.english.global.jpa.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class Address {

    private String street;
    private String detail;
    private String postalCode;

    @Builder
    public Address(String street, String detail, String postalCode) {
        this.street = street;
        this.detail = detail;
        this.postalCode = postalCode;
    }
}
