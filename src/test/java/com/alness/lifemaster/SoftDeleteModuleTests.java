package com.alness.lifemaster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.repository.DebtsRespository;
import com.alness.lifemaster.debts.service.impl.DebtsServiceImpl;
import com.alness.lifemaster.nutrition.entity.NutritionEntity;
import com.alness.lifemaster.nutrition.repository.NutritionRepository;
import com.alness.lifemaster.nutrition.service.impl.NutritionServiceImpl;

@ExtendWith(MockitoExtension.class)
class SoftDeleteModuleTests {

    @Mock
    private DebtsRespository debtsRepository;

    @InjectMocks
    private DebtsServiceImpl debtsService;

    @Mock
    private NutritionRepository nutritionRepository;

    @InjectMocks
    private NutritionServiceImpl nutritionService;

    @Test
    void debtDeletionOnlyTargetsAnActiveDebtOwnedByTheUser() {
        UUID userId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        DebtsEntity debt = new DebtsEntity();
        debt.setId(debtId);
        debt.setErased(false);
        when(debtsRepository.findByIdAndUserIdAndErasedFalse(debtId, userId))
                .thenReturn(Optional.of(debt));

        debtsService.delete(userId.toString(), debtId.toString());

        assertThat(debt.getErased()).isTrue();
        verify(debtsRepository).save(debt);
    }

    @Test
    void nutritionDeletionOnlyTargetsAnActiveRecordOwnedByTheUser() {
        UUID userId = UUID.randomUUID();
        UUID nutritionId = UUID.randomUUID();
        NutritionEntity nutrition = new NutritionEntity();
        nutrition.setId(nutritionId);
        nutrition.setErased(false);
        when(nutritionRepository.findByIdAndUserIdAndErasedFalse(nutritionId, userId))
                .thenReturn(Optional.of(nutrition));

        nutritionService.delete(userId.toString(), nutritionId.toString());

        assertThat(nutrition.getErased()).isTrue();
        verify(nutritionRepository).save(nutrition);
    }
}
