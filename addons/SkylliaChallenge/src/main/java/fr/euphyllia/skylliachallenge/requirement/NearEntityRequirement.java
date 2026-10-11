package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record NearEntityRequirement(EntityType type, int amount, double radius) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        if (Bukkit.isOwnedByCurrentRegion(player)) {
            return count(player) >= amount;
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        boolean scheduled = player.getScheduler().execute(
                SkylliaChallenge.getInstance(),
                () -> future.complete(count(player) >= amount),
                () -> future.complete(false),
                1L
        );
        if (!scheduled) return false;
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    private int count(Player player) {
        Location loc = player.getLocation();
        return player.getWorld().getNearbyEntities(loc, radius, radius, radius, e -> e.getType() == type).size();
    }

    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.nearby_entity.display", Map.of(
                "%entity_type%", type.name(),
                "%radius%", String.valueOf((int) radius),
                "%amount%", String.valueOf(amount)
        ), false);
    }
}
