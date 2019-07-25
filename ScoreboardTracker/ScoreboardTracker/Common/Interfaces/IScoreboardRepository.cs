using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using ScoreboardTracker.Models;

namespace ScoreboardTracker.Common.Interfaces
{
    public interface IScoreboardRepository
    {
        Task<List<Group>> GetGroups();
        Task<Group> GetGroup();
        Task<List<Game>> GetGames(string groupId);
        Task<Game> GetOnGoingGame(string groupId);

        Task<bool> AddGame(string groupId, Game game);
        Task<bool> UpdateGame(string groupId, Game game);
        Task<bool> DeleteGame(string groupId, string gameId);


        Task<List<User>> GetAllUsers();

    }
}
