package com.wardenfar.osm2map.command.provider;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;
import com.wardenfar.osm2map.map.entity.JtsZone;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ZoneTypeProvider implements Provider<JtsZone.Type> {

    @Override
    public boolean isProvided() {
        return false;
    }

    @Override
    public JtsZone.Type get(CommandArgs commandArgs, List<? extends Annotation> list) throws ArgumentException, ProvisionException {
        return JtsZone.Type.valueOf(commandArgs.next());
    }

    @Override
    public List<String> getSuggestions(String s) {
        return Arrays.stream(JtsZone.Type.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
