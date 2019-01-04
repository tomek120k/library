package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBook
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.*
import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.onlyResearcherPatronsCanPlaceOpenEndedHolds

class PatronRequestingOpenEndedHoldTest extends Specification {

    Instant from = Instant.MIN

    def 'researcher patron can request close ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        and:
            PatronId patronId = anyPatronId()
        and:
            PatronBooks researcherPatron = researcherPatronWithPolicy(patronId, onlyResearcherPatronsCanPlaceOpenEndedHolds)
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = researcherPatron.placeOnHold(aBook, HoldDuration.forOpenEnded(from))
        then:
            hold.isRight()
            hold.get().with {
                BookPlacedOnHold bookPlacedOnHold = it.bookPlacedOnHold
                assert bookPlacedOnHold.libraryBranchId == aBook.libraryBranch.libraryBranchId
                assert bookPlacedOnHold.patronId == patronId.patronId
                assert bookPlacedOnHold.bookId == aBook.bookInformation.bookId.bookId
                assert bookPlacedOnHold.holdFrom == from
                assert bookPlacedOnHold.holdTill == null
            }

    }

    def 'regular patron cannot request open ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        and:
            PatronId patronId = anyPatronId()
        and:
            PatronBooks regularPatron = regularPatronWithPolicy(patronId, onlyResearcherPatronsCanPlaceOpenEndedHolds)
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = regularPatron.placeOnHold(aBook, HoldDuration.forOpenEnded(from))
        then:
            hold.isLeft()
            hold.getLeft().with {
                assert it.reason.contains("regular patron cannot place open ended holds")
                assert it.libraryBranchId == aBook.libraryBranch.libraryBranchId
                assert it.patronId == patronId.patronId
                assert it.bookId == aBook.bookInformation.bookId.bookId
            }

    }


}