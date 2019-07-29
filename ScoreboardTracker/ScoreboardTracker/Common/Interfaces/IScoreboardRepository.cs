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
        Task<List<Game>> GetCompletedGames(string groupId);
        Task<bool> AddGame(string groupId, Game game);
        Task<bool> UpdateGame(string groupId, Game game);
        Task<bool> DeleteGame(string groupId, string gameId);
        void StartGameScoreListner(string groupId, IGameScoreChangeListener listener);


        Task<List<User>> GetAllUsers();

    }
}
