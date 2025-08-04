package com.alness.lifemaster.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.assistance.entity.AssistanceEntity;
import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.income.dto.request.IncomeRequest;
import com.alness.lifemaster.income.entity.IncomeEntity;
import com.alness.lifemaster.utils.DateTimeUtils;

import jakarta.annotation.PostConstruct;

@Component
public class GenericMapper {
    private final ModelMapper mapper;

    public GenericMapper() {
        this.mapper = new ModelMapper();
    }

    @PostConstruct
    public void init() {
        configureModelMapper();
        registerConverters();
        registerMappings();
    }

    // Configuración general del ModelMapper
    private void configureModelMapper() {
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);
    }

    // Método para registrar convertidores personalizados
    private void registerConverters() {
        Converter<String, LocalDate> stringToLocalDate = createConverter(DateTimeUtils::parseToLocalDate);
        Converter<String, LocalTime> stringToLocalTime = createConverter(DateTimeUtils::parseToLocalTime);
        Converter<String, LocalDateTime> stringToLocalDateTime = createConverter(DateTimeUtils::parseToLocalDateTime);
        Converter<LocalTime, String> localTimeToString = createConverter(
                DateTimeUtils::parseLocalTimeToString);
        mapper.addConverter(stringToLocalDate);
        mapper.addConverter(stringToLocalDateTime);
        mapper.addConverter(stringToLocalTime);
        mapper.addConverter(localTimeToString);
    }

    // Método para registrar mapeos específicos
    private void registerMappings() {

        Converter<String, LocalDate> stringToLocalDate = createConverter(DateTimeUtils::parseToLocalDate);
        Converter<String, LocalTime> stringToLocalTime = createConverter(DateTimeUtils::parseToLocalTime);
        Converter<LocalTime, String> localTimeToString = createConverter(
                DateTimeUtils::parseLocalTimeToString);

        mapper.createTypeMap(AssistanceRequest.class, AssistanceEntity.class)
                .addMappings(mpi -> {
                    mpi.using(stringToLocalTime).map(AssistanceRequest::getDepartureTime,
                            AssistanceEntity::setDepartureTime);
                    mpi.using(stringToLocalTime).map(AssistanceRequest::getTimeEntry, AssistanceEntity::setTimeEntry);
                    mpi.using(stringToLocalDate).map(AssistanceRequest::getWorkDate, AssistanceEntity::setWorkDate);
                });
        mapper.createTypeMap(AssistanceEntity.class, AssistanceResponse.class)
                .addMappings(amp -> {
                    amp.using(localTimeToString).map(AssistanceEntity::getDepartureTime,
                            AssistanceResponse::setDepartureTime);
                    amp.using(localTimeToString).map(AssistanceEntity::getTimeEntry,
                            AssistanceResponse::setTimeEntry);
                });

        mapper.createTypeMap(ExpensesRequest.class, ExpensesEntity.class)
                .addMappings(
                        mpa -> mpa.using(stringToLocalDate).map(ExpensesRequest::getPaymentDate,
                                ExpensesEntity::setPaymentDate));

        mapper.createTypeMap(IncomeRequest.class, IncomeEntity.class)
                .addMappings(
                        mpa -> mpa.using(stringToLocalDate).map(IncomeRequest::getPaymentDate,
                                IncomeEntity::setPaymentDate));
    }

    // Método para mapear un objeto
    public <T, R> R map(T source, Class<R> targetClass) {
        try {
            return mapper.map(source, targetClass);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MappingException(
                    "Error al mapear " + source.getClass().getSimpleName() + " a " + targetClass.getSimpleName(), e);
        }
    }

    public <S, D> D map(S source, D destination) {
        try {
            mapper.map(source, destination);
            return destination;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MappingException(
                    "Error al mapear de " + source.getClass().getSimpleName() +
                            " a " + destination.getClass().getSimpleName(),
                    e);
        }
    }

    // Método para mapear una lista de objetos
    public <T, R> List<R> mapList(List<T> sourceList, Class<R> targetClass) {
        return sourceList.stream()
                .map(element -> map(element, targetClass))
                .toList();
    }

    // Método genérico para crear convertidores personalizados
    private <S, D> Converter<S, D> createConverter(Function<S, D> converterFunction) {
        return new AbstractConverter<>() {
            @Override
            protected D convert(S source) {
                return source == null ? null : converterFunction.apply(source);
            }
        };
    }

    // Método para agregar configuraciones personalizadas al ModelMapper
    public void addCustomMapping(CustomMapping customMapping) {
        customMapping.configure(mapper);
    }

    // Excepción personalizada para el mapeo
    public static class MappingException extends RuntimeException {
        public MappingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Interfaz funcional para configuraciones personalizadas
    @FunctionalInterface
    public interface CustomMapping {
        void configure(ModelMapper modelMapper);
    }
}
