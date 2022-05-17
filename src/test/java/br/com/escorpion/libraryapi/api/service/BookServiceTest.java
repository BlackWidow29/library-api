package br.com.escorpion.libraryapi.api.service;

import br.com.escorpion.libraryapi.api.model.entity.Book;
import br.com.escorpion.libraryapi.api.repository.BookRepository;
import br.com.escorpion.libraryapi.api.service.impl.BookServiceImpl;
import br.com.escorpion.libraryapi.exception.BusinessException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);

    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {

        var book = createValidBook();

        var savedBookMock = Book.builder()
                .id(1L)
                .author("Autor")
                .title("Meu Livro")
                .isbn("1213")
                .build();

        when(bookRepository.save(book)).thenReturn(savedBookMock);

        var savedBook = bookService.save(book);

        assertNotNull(savedBook.getId());
        assertEquals("1213", savedBook.getIsbn());
        assertEquals("Autor", savedBook.getAuthor());
        assertEquals("Meu Livro", savedBook.getTitle());
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado.")
    public void shouldNotSaveBookWithDuplicateISBN() {
        //cenario
        Book validBook = createValidBook();
        when(bookRepository.existsByIsbn(Mockito.anyString()))
                .thenReturn(true);

        //execucao
        String mensagemErro = "Isbn já cadastrado";

        Throwable exception = Assertions.catchThrowable(() -> bookService.save(validBook));
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage(mensagemErro);
        Mockito.verify(bookRepository, Mockito.never()).save(validBook);
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void getByIdTest() {
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> optionalBook = bookService.getById(id);

        assertThat(optionalBook.isPresent()).isTrue();
        assertThat(optionalBook.get().getId()).isEqualTo(id);
        assertThat(optionalBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(optionalBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(optionalBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existe na base.")
    public void bookNotFoundByIdTest() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> optionalBook = bookService.getById(id);

        assertThat(optionalBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        Long id = 1L;

        Book updatingBook = Book.builder().id(id).isbn("1213").build();

        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        Book book = bookService.update(updatingBook);

        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
    }

    @Test
    @DisplayName("Deve ocorrer um erro ao tentar atualizar um livro inexistente.")
    public void updateInvalidBookTest() {
        Book book = new Book();
        assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve deletar um livro com sucesso")
    public void deleteBookTest() {
        Book validBook = createValidBook();
        validBook.setId(1L);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(validBook));

        Mockito.verify(bookRepository, Mockito.times(1)).delete(validBook);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar livro inexistente")
    public void deleteinvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void findByIdTest() {
        Long id = 1L;
        Book validBook = createValidBook();

        validBook.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(validBook));

        Optional<Book> foundBook = bookService.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getIsbn()).isEqualTo(validBook.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(validBook.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(validBook.getTitle());
    }

    @Test
    @DisplayName("Deve filtrar os livros pelas propriedades")
    public void findBookTest() {

        //cenario
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> lista = List.of(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
        when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        //execucao
        Page<Book> result = bookService.find(book, pageRequest);

        //verificacoes
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve obter livro pelo isbn")
    public void getBookByIsbnTest(){
        String isbn = "1213";
        Book book = createValidBook();
        book.setId(1L);

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Optional<Book> optionalBook = bookService.getBookByIsbn(isbn);

        assertThat(optionalBook.isPresent()).isTrue();
        assertThat(optionalBook.get().getId()).isEqualTo(1L);
        assertThat(optionalBook.get().getIsbn()).isEqualTo("1213");

        verify(bookRepository, times(1)).findByIsbn(isbn);
    }

    private Book createValidBook() {
        return Book.builder()
                .author("Autor")
                .title("Meu Livro")
                .isbn("1213")
                .build();
    }

    private Book updatedBook() {
        return Book.builder()
                .author("Autor Atualizado")
                .title("Meu Livro Atualizado")
                .isbn("1213")
                .build();
    }
}