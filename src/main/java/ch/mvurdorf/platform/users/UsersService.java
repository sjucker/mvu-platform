package ch.mvurdorf.platform.users;

import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Comparator.comparing;

@Service
class UsersService {

    private final UsersRepository usersRepository;

    UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    List<UserDto> findAll() {
        return usersRepository.findAll().stream()
                              .map(l -> new UserDto(l.getEmail(), l.getName(), l.getActive()))
                              .sorted(comparing(UserDto::name))
                              .toList();
    }
}
