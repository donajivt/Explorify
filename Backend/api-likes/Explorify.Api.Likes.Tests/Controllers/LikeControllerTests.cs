using Explorify.Api.Likes.Application.Dtos;
using Explorify.Api.Likes.Application.Interfaces;
using Explorify.Api.Likes.Controllers;
using FluentAssertions;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Moq;
using System.Security.Claims;
using Xunit;

namespace Explorify.Api.Likes.Tests.Unit.Controllers
{
    public class LikeControllerTests
    {
        private readonly Mock<ILikeService> _mockService;
        private readonly LikeController _controller;
        private const string TestUserId = "507f1f77bcf86cd799439012";

        public LikeControllerTests()
        {
            _mockService = new Mock<ILikeService>();
            _controller = new LikeController(_mockService.Object);

            // Configurar el contexto HTTP con un usuario autenticado
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, TestUserId)
            };
            var identity = new ClaimsIdentity(claims, "TestAuth");
            var claimsPrincipal = new ClaimsPrincipal(identity);

            _controller.ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext { User = claimsPrincipal }
            };
        }

        [Fact]
        public async Task ToggleLike_WhenUserHasNotLiked_AddsLike()
        {
            // Arrange
            var request = new LikeRequestDto { PublicationId = "507f1f77bcf86cd799439011" };
            var likeDto = new LikeDto
            {
                Id = "507f1f77bcf86cd799439013",
                PublicationId = request.PublicationId,
                UserId = TestUserId,
                CreatedAt = DateTime.UtcNow
            };

            _mockService
                .Setup(s => s.UserHasLikedAsync(request.PublicationId, TestUserId))
                .ReturnsAsync(false);

            _mockService
                .Setup(s => s.AddLikeAsync(request.PublicationId, TestUserId))
                .ReturnsAsync(likeDto);

            // Act
            var result = await _controller.ToggleLike(request);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
            response.Message.Should().Be("Like agregado");

            _mockService.Verify(s => s.AddLikeAsync(request.PublicationId, TestUserId), Times.Once);
            _mockService.Verify(s => s.RemoveLikeAsync(It.IsAny<string>(), It.IsAny<string>()), Times.Never);
        }

        [Fact]
        public async Task ToggleLike_WhenUserHasLiked_RemovesLike()
        {
            // Arrange
            var request = new LikeRequestDto { PublicationId = "507f1f77bcf86cd799439011" };

            _mockService
                .Setup(s => s.UserHasLikedAsync(request.PublicationId, TestUserId))
                .ReturnsAsync(true);

            _mockService
                .Setup(s => s.RemoveLikeAsync(request.PublicationId, TestUserId))
                .ReturnsAsync(true);

            // Act
            var result = await _controller.ToggleLike(request);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
            response.Message.Should().Be("Like eliminado");

            _mockService.Verify(s => s.RemoveLikeAsync(request.PublicationId, TestUserId), Times.Once);
            _mockService.Verify(s => s.AddLikeAsync(It.IsAny<string>(), It.IsAny<string>()), Times.Never);
        }

        [Fact]
        public async Task ToggleLike_WhenExceptionOccurs_ReturnsBadRequest()
        {
            // Arrange
            var request = new LikeRequestDto { PublicationId = "507f1f77bcf86cd799439011" };

            _mockService
                .Setup(s => s.UserHasLikedAsync(It.IsAny<string>(), It.IsAny<string>()))
                .ThrowsAsync(new Exception("Test exception"));

            // Act
            var result = await _controller.ToggleLike(request);

            // Assert
            var badRequestResult = result.Result.Should().BeOfType<BadRequestObjectResult>().Subject;
            var response = badRequestResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeFalse();
            response.Message.Should().Be("Test exception");
        }

        [Fact]
        public async Task GetLikeStats_WithAuthenticatedUser_ReturnsStats()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var stats = new LikeStatsDto
            {
                PublicationId = publicationId,
                TotalLikes = 42,
                UserHasLiked = true
            };

            _mockService
                .Setup(s => s.GetLikeStatsAsync(publicationId, TestUserId))
                .ReturnsAsync(stats);

            // Act
            var result = await _controller.GetLikeStats(publicationId);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
            response.Result.Should().BeEquivalentTo(stats);
        }

        [Fact]
        public async Task GetLikeStats_WithUnauthenticatedUser_ReturnsStatsWithEmptyUserId()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var stats = new LikeStatsDto
            {
                PublicationId = publicationId,
                TotalLikes = 42,
                UserHasLiked = false
            };

            // Configurar usuario no autenticado
            _controller.ControllerContext.HttpContext.User = new ClaimsPrincipal(new ClaimsIdentity());

            _mockService
                .Setup(s => s.GetLikeStatsAsync(publicationId, string.Empty))
                .ReturnsAsync(stats);

            // Act
            var result = await _controller.GetLikeStats(publicationId);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();

            _mockService.Verify(s => s.GetLikeStatsAsync(publicationId, string.Empty), Times.Once);
        }

        [Fact]
        public async Task GetLikesByPublication_ReturnsAllLikes()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var likes = new List<LikeDto>
            {
                new LikeDto { Id = "1", PublicationId = publicationId, UserId = "user1", CreatedAt = DateTime.UtcNow },
                new LikeDto { Id = "2", PublicationId = publicationId, UserId = "user2", CreatedAt = DateTime.UtcNow }
            };

            _mockService
                .Setup(s => s.GetLikesByPublicationAsync(publicationId))
                .ReturnsAsync(likes);

            // Act
            var result = await _controller.GetLikesByPublication(publicationId);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
            (response.Result as IEnumerable<LikeDto>).Should().HaveCount(2);
        }

        [Fact]
        public async Task GetLikesByUser_ReturnsUserLikes()
        {
            // Arrange
            var userId = "507f1f77bcf86cd799439012";
            var likes = new List<LikeDto>
            {
                new LikeDto { Id = "1", PublicationId = "pub1", UserId = userId, CreatedAt = DateTime.UtcNow },
                new LikeDto { Id = "2", PublicationId = "pub2", UserId = userId, CreatedAt = DateTime.UtcNow }
            };

            _mockService
                .Setup(s => s.GetLikesByUserAsync(userId))
                .ReturnsAsync(likes);

            // Act
            var result = await _controller.GetLikesByUser(userId);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
            (response.Result as IEnumerable<LikeDto>).Should().HaveCount(2);
        }

        [Fact]
        public async Task HasLiked_WhenUserHasLiked_ReturnsTrue()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";

            _mockService
                .Setup(s => s.UserHasLikedAsync(publicationId, TestUserId))
                .ReturnsAsync(true);

            // Act
            var result = await _controller.HasLiked(publicationId);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
        }

        [Fact]
        public async Task HasLiked_WhenUserHasNotLiked_ReturnsFalse()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";

            _mockService
                .Setup(s => s.UserHasLikedAsync(publicationId, TestUserId))
                .ReturnsAsync(false);

            // Act
            var result = await _controller.HasLiked(publicationId);

            // Assert
            var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
            var response = okResult.Value.Should().BeOfType<ResponseDto>().Subject;
            response.IsSuccess.Should().BeTrue();
        }
    }
}