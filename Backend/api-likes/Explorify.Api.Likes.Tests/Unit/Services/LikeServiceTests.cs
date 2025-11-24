using Explorify.Api.Likes.Application.Interfaces;
using Explorify.Api.Likes.Application.Services;
using Explorify.Api.Likes.Domain.Entities;
using FluentAssertions;
using Moq;
using Xunit;

namespace Explorify.Api.Likes.Tests.Unit.Services
{
    public class LikeServiceTests
    {
        private readonly Mock<ILikeRepository> _mockRepository;
        private readonly LikeService _service;

        public LikeServiceTests()
        {
            _mockRepository = new Mock<ILikeRepository>();
            _service = new LikeService(_mockRepository.Object);
        }

        [Fact]
        public async Task AddLikeAsync_WhenLikeDoesNotExist_CreatesNewLike()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync((Like?)null);

            _mockRepository
                .Setup(r => r.CreateAsync(It.IsAny<Like>()))
                .Returns(Task.CompletedTask);

            // Act
            var result = await _service.AddLikeAsync(publicationId, userId);

            // Assert
            result.Should().NotBeNull();
            result.PublicationId.Should().Be(publicationId);
            result.UserId.Should().Be(userId);
            result.CreatedAt.Should().BeCloseTo(DateTime.UtcNow, TimeSpan.FromSeconds(5));

            _mockRepository.Verify(r => r.CreateAsync(It.IsAny<Like>()), Times.Once);
        }

        [Fact]
        public async Task AddLikeAsync_WhenLikeAlreadyExists_ReturnsExistingLike()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";
            var existingLike = new Like
            {
                Id = "507f1f77bcf86cd799439013",
                PublicationId = publicationId,
                UserId = userId,
                CreatedAt = DateTime.UtcNow.AddDays(-1)
            };

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync(existingLike);

            // Act
            var result = await _service.AddLikeAsync(publicationId, userId);

            // Assert
            result.Should().NotBeNull();
            result.Id.Should().Be(existingLike.Id);
            result.PublicationId.Should().Be(existingLike.PublicationId);
            result.UserId.Should().Be(existingLike.UserId);
            result.CreatedAt.Should().Be(existingLike.CreatedAt);

            _mockRepository.Verify(r => r.CreateAsync(It.IsAny<Like>()), Times.Never);
        }

        [Fact]
        public async Task RemoveLikeAsync_WhenLikeExists_DeletesLikeAndReturnsTrue()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";
            var existingLike = new Like
            {
                Id = "507f1f77bcf86cd799439013",
                PublicationId = publicationId,
                UserId = userId,
                CreatedAt = DateTime.UtcNow
            };

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync(existingLike);

            _mockRepository
                .Setup(r => r.DeleteByUserAndPublicationAsync(userId, publicationId))
                .Returns(Task.CompletedTask);

            // Act
            var result = await _service.RemoveLikeAsync(publicationId, userId);

            // Assert
            result.Should().BeTrue();
            _mockRepository.Verify(r => r.DeleteByUserAndPublicationAsync(userId, publicationId), Times.Once);
        }

        [Fact]
        public async Task RemoveLikeAsync_WhenLikeDoesNotExist_ReturnsFalse()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync((Like?)null);

            // Act
            var result = await _service.RemoveLikeAsync(publicationId, userId);

            // Assert
            result.Should().BeFalse();
            _mockRepository.Verify(r => r.DeleteByUserAndPublicationAsync(It.IsAny<string>(), It.IsAny<string>()), Times.Never);
        }

        [Fact]
        public async Task GetLikeStatsAsync_ReturnsCorrectStats()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";
            var totalLikes = 42;

            _mockRepository
                .Setup(r => r.CountByPublicationIdAsync(publicationId))
                .ReturnsAsync(totalLikes);

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync(new Like { Id = "507f1f77bcf86cd799439013" });

            // Act
            var result = await _service.GetLikeStatsAsync(publicationId, userId);

            // Assert
            result.Should().NotBeNull();
            result.PublicationId.Should().Be(publicationId);
            result.TotalLikes.Should().Be(totalLikes);
            result.UserHasLiked.Should().BeTrue();
        }

        [Fact]
        public async Task GetLikesByPublicationAsync_ReturnsAllLikes()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var likes = new List<Like>
            {
                new Like { Id = "1", PublicationId = publicationId, UserId = "user1", CreatedAt = DateTime.UtcNow },
                new Like { Id = "2", PublicationId = publicationId, UserId = "user2", CreatedAt = DateTime.UtcNow },
                new Like { Id = "3", PublicationId = publicationId, UserId = "user3", CreatedAt = DateTime.UtcNow }
            };

            _mockRepository
                .Setup(r => r.GetByPublicationIdAsync(publicationId))
                .ReturnsAsync(likes);

            // Act
            var result = await _service.GetLikesByPublicationAsync(publicationId);

            // Assert
            result.Should().HaveCount(3);
            result.Should().AllSatisfy(dto => dto.PublicationId.Should().Be(publicationId));
        }

        [Fact]
        public async Task GetLikesByUserAsync_ReturnsUserLikes()
        {
            // Arrange
            var userId = "507f1f77bcf86cd799439012";
            var likes = new List<Like>
            {
                new Like { Id = "1", PublicationId = "pub1", UserId = userId, CreatedAt = DateTime.UtcNow },
                new Like { Id = "2", PublicationId = "pub2", UserId = userId, CreatedAt = DateTime.UtcNow }
            };

            _mockRepository
                .Setup(r => r.GetByUserIdAsync(userId))
                .ReturnsAsync(likes);

            // Act
            var result = await _service.GetLikesByUserAsync(userId);

            // Assert
            result.Should().HaveCount(2);
            result.Should().AllSatisfy(dto => dto.UserId.Should().Be(userId));
        }

        [Fact]
        public async Task UserHasLikedAsync_WhenLikeExists_ReturnsTrue()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync(new Like { Id = "507f1f77bcf86cd799439013" });

            // Act
            var result = await _service.UserHasLikedAsync(publicationId, userId);

            // Assert
            result.Should().BeTrue();
        }

        [Fact]
        public async Task UserHasLikedAsync_WhenLikeDoesNotExist_ReturnsFalse()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var userId = "507f1f77bcf86cd799439012";

            _mockRepository
                .Setup(r => r.GetByUserAndPublicationAsync(userId, publicationId))
                .ReturnsAsync((Like?)null);

            // Act
            var result = await _service.UserHasLikedAsync(publicationId, userId);

            // Assert
            result.Should().BeFalse();
        }
    }
}