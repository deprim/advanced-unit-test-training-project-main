package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.audit.AuditLogger;
import lv.bootcamp.shelter.audit.RejectionReason;
import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.model.*;
import lv.bootcamp.shelter.repository.AdopterRepository;
import lv.bootcamp.shelter.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdoptionEligibilityServiceTest {

    @Mock
    private AdopterRepository adopterRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    NotificationClient notificationClient;

    @Mock
    AuditLogger auditLogger;

    @InjectMocks
    private AdoptionEligibilityService service;


    @Test
    void evaluateAdoption_shouldRejectWhenAdopterNotFound() {
        when(adopterRepository.findById(1L)).thenReturn(Optional.empty());

        AdoptionResult result = service.evaluateAdoption(1L, 1L);

        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.ADOPTER_NOT_FOUND));
        verify(adopterRepository).findById(1L);
        verifyNoInteractions(animalRepository, notificationClient, auditLogger);
    }

    @Test
    void evaluateAdoption_shouldRejectWhenAnimalNotFound() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                false,
                false);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.empty());

        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.ANIMAL_NOT_FOUND));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verifyNoInteractions(notificationClient, auditLogger);
    }

    @Test
    void evaluateAdoption_shouldRejectWhenAnimalReserved() {

        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.RESERVED);


        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.ANIMAL_NOT_AVAILABLE));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verifyNoInteractions(notificationClient, auditLogger);

    }

    @Test
    void evaluateAdoption_shouldRejectWhenAnimalAdopted() {

        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.ADOPTED);


        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.ANIMAL_NOT_AVAILABLE));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verifyNoInteractions(notificationClient, auditLogger);

    }

    @Test
    void evaluateAdoption_shouldRejectWhenAdopterIsUnderage() {

        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                16,
                0,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.UNDERAGE));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logRejection(1L, 1L, RejectionReason.UNDERAGE);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void evaluateAdoption_shouldRejectWhenPetLimitReached() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                3,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.PET_LIMIT_REACHED));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logRejection(1L, 1L, RejectionReason.PET_LIMIT_REACHED);
        verifyNoInteractions(notificationClient);

    }

    @Test
    void evaluateAdoption_shouldRejectWhenLargePropertyPetLimitReached() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                5,
                0,
                true,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.PET_LIMIT_REACHED));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logRejection(1L, 1L, RejectionReason.PET_LIMIT_REACHED);
        verifyNoInteractions(notificationClient);

    }

    @Test
    void valuateAdoption_shouldRejectBirdWithoutPermission() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                true,
                false);

        Animal animal = new Animal(1L,
                "Kesha",
                AnimalType.BIRD,
                "Speaking",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.EXOTIC_PERMIT_REQUIRED));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logRejection(1L, 1L, RejectionReason.NO_EXOTIC_PERMIT);
        verifyNoInteractions(notificationClient);

    }

    @Test
    void valuateAdoption_shouldRejectRabbitWithoutPermission() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                true,
                false);

        Animal animal = new Animal(1L,
                "Kesha",
                AnimalType.RABBIT,
                "Grey",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.rejected(RejectionReasons.EXOTIC_PERMIT_REQUIRED));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logRejection(1L, 1L, RejectionReason.NO_EXOTIC_PERMIT);
        verifyNoInteractions(notificationClient);

    }

    @Test
    void valuateAdoption_shouldApproveEligibleAdopter() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                true,
                true);

        Animal animal = new Animal(1L,
                "Kesha",
                AnimalType.RABBIT,
                "Grey",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.approved(15));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logApproval(1L, 1L, 15);
        verify(notificationClient).sendApprovalNotification("test@gmail.com", "Kesha");
    }

    // After first Mutation test

    @Test
    void evaluateAdoption_shouldApproveAdultWithFourPetsAndLargeProperty() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                18,
                4,
                0,
                true,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        when(adopterRepository.findById(1L)).thenReturn(Optional.of(adopter));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        AdoptionResult result = service.evaluateAdoption(1L, 1L);
        assertThat(result).isEqualTo(AdoptionResult.approved(7));
        verify(adopterRepository).findById(1L);
        verify(animalRepository).findById(1L);
        verify(auditLogger).logApproval(1L, 1L, 7);
        verify(notificationClient).sendApprovalNotification("test@gmail.com", "Rex");

    }

    @Test
    void calculatePriorityScore_shouldAddTenPointsForPreviousAdoption() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                1,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        int priorityScore = service.calculatePriorityScore(adopter, animal);
        assertThat(priorityScore).isEqualTo(10);

    }

    @Test
    void calculatePriorityScore_shouldAddFivePointsForFourPreviousAdoptions() {
        // 4 > 0 -> true -> +10
        // 4 > 3 -> true -> +5

        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                4,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        int priorityScore = service.calculatePriorityScore(adopter, animal);
        assertThat(priorityScore).isEqualTo(15);

    }

    @Test
    void calculatePriorityScore_shouldNotAddFivePointsForThreePreviousAdoptions() {
        // 3 > 0 -> true -> +10
        // 3 > 3 -> false -> 0
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                3,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        int priorityScore = service.calculatePriorityScore(adopter, animal);
        assertThat(priorityScore).isEqualTo(10);

    }

    @Test
    void calculatePriorityScore_shouldAddTwentyPointsForSeniorAnimal() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                8, // senior
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        int priorityScore = service.calculatePriorityScore(adopter, animal);
        assertThat(priorityScore).isEqualTo(20);
    }

    @Test
    void calculatePriorityScore_shouldNotAddTwentyPointsForAnimalAtAgeSeven() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                7,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        int priorityScore = service.calculatePriorityScore(adopter, animal);
        assertThat(priorityScore).isZero();
    }

    @Test
    void calculatePriorityScore_shouldReturnZeroWhenAnimalAgeIsNull() {
        Adopter adopter = new Adopter(1L,
                "Daniils",
                "test@gmail.com",
                24,
                0,
                0,
                false,
                false);

        Animal animal = new Animal(1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                null,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        int priorityScore = service.calculatePriorityScore(adopter, animal);
        assertThat(priorityScore).isZero();

    }


}
