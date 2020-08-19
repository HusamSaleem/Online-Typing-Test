using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class TypingTestClient : MonoBehaviour
{
    public TextMeshProUGUI words;
    public InputField userInput;

    public Text timer;
    public Text userWPMText;
    public Text userAccText;
    public Text player1WPMText;
    public Text player1AccText;
    public Text player2WPMText;
    public Text player2AccText;
    public Text gameLog;

    private string cleanWords;

    private bool canSendData = true;

    private int DATA_SENDING_INTERVAL = 5;

    private bool canDecreaseTime = false;
    private int timeRemainingCounter = -1;

    private bool gameLoaded = false;

    public static string uiPlayerName;

    private bool sentFinishedPacket = false;

    private void Update()
    {
        // If client has recieved server data regarding the player statistics, update that information
        if (GameManager.infoUpdateNeeded)
        {
            updateUI();
            GameManager.infoUpdateNeeded = false;
        }

        // Starts the game..
        if (GameManager.gameStartedUIUpdate)
        {
            StopAllCoroutines();
            GameManager.gameStartedUIUpdate = false;
            startGame();
        }

        if (GameManager.gameComplete)
        {
            timer.text = "Game has finished!";
            GameManager.gameComplete = false;
            StopAllCoroutines();
            showWinner();

            gameLoaded = false;
            userInput.gameObject.SetActive(false);
            canSendData = true;
            canDecreaseTime = true;
            GameManager.playerStats.Clear();
        }

        if (canSendData && GameManager.gameStarted)
        {
            StartCoroutine(sendInputToServer());
        }

        if (canDecreaseTime && GameManager.gameStarted)
        {
            StartCoroutine(UpdateTimer());
        }
    }

    public void showWinner()
    {
        if (gameObject.activeInHierarchy)
        {
            ClientData[] entries = new ClientData[2];
            GameManager.playerStats.Values.CopyTo(entries, 0);

            if (GameManager.soloGame)
            {
                gameLog.color = Color.green;
                gameLog.text = entries[0].Name + " had an Estimated " + (int)float.Parse(entries[0].WPM) + " Words Per Minute!";
                return;
            }

            if (entries[0].WPM == entries[1].WPM)
            {
                gameLog.text = "It is a tie!";
                return;
            }
            else if ((int)float.Parse(entries[0].WPM) > (int)float.Parse(entries[1].WPM))
            {
                gameLog.color = Color.green;
                gameLog.text = "Winner: " + entries[0].Name + " with an Estimated " + (int)float.Parse(entries[0].WPM) + " Words Per Minute!";
            }
            else if ((int)float.Parse(entries[0].WPM) < (int)float.Parse(entries[1].WPM))
            {
                gameLog.color = Color.green;
                gameLog.text = "Winner: " + entries[1].Name + " with an Estimated " + (int)float.Parse(entries[1].WPM) + " Words Per Minute!";
            }
        }
    }

    public void startGame()
    {
        sentFinishedPacket = false;
        gameLoaded = false;
        userInput.gameObject.SetActive(false);
        canSendData = true;
        canDecreaseTime = true;
        GameManager.playerStats.Clear();

        userWPMText.gameObject.SetActive(false);
        userAccText.gameObject.SetActive(false);
        player1WPMText.gameObject.SetActive(false);
        player1AccText.gameObject.SetActive(false);
        player2WPMText.gameObject.SetActive(false);
        player2AccText.gameObject.SetActive(false);

        gameLog.text = "";
        userInput.text = "";
        userInput.gameObject.SetActive(true);

        timeRemainingCounter = GameManager.startTime;
        words.text = GameManager.wordList;
        userInput.characterLimit = words.text.Length;
        cleanWords = words.text;

        canDecreaseTime = true;
        timer.text = timeRemainingCounter + " Seconds left";
        gameLoaded = true;
    }

    private void LateUpdate()
    {
        // Focuses the user input field if clicked away
        if (!userInput.isFocused)
        {
            userInput.ActivateInputField();
            userInput.caretPosition = userInput.text.Length;
            userInput.ForceLabelUpdate();
        }
    }

    public IEnumerator sendInputToServer()
    {
        canSendData = false;
        if (gameLoaded && userInput.text.Length < cleanWords.Length)
        {
            ServerAPI.SendData("Input Update: " + userInput.text);
        }
        else if (gameLoaded && userInput.text.Length >= cleanWords.Length && !sentFinishedPacket)
        {
            gameLog.text = "You have completed the test. Please wait for the game and other players to finish";
            ServerAPI.SendData("Input Update: " + userInput.text);
            ServerAPI.SendData("Word List Finished");
            userInput.gameObject.SetActive(false);
            sentFinishedPacket = true;
        }

        yield return new WaitForSeconds(DATA_SENDING_INTERVAL);
        canSendData = true;
    }

    public IEnumerator UpdateTimer()
    {
        canDecreaseTime = false;
        timeRemainingCounter--;
        timer.text = timeRemainingCounter + " Seconds left";

        yield return new WaitForSeconds(1);
        canDecreaseTime = true;
    }

    public void updateUI()
    {
        userWPMText.gameObject.SetActive(true);
        userAccText.gameObject.SetActive(true);
        player1WPMText.gameObject.SetActive(true);
        player1AccText.gameObject.SetActive(true);
        player2WPMText.gameObject.SetActive(true);
        player2AccText.gameObject.SetActive(true);

        if (GameManager.soloGame)
        {
            player2WPMText.gameObject.SetActive(false);
            player2AccText.gameObject.SetActive(false);
        }

        foreach (KeyValuePair<string, ClientData> entry in GameManager.playerStats)
        {
            int wpm = Mathf.Max(Mathf.RoundToInt(float.Parse(entry.Value.WPM)), 0);
            int accuracy = Mathf.Max(Mathf.RoundToInt(float.Parse(entry.Value.Accuracy)), 0);
            timeRemainingCounter = (int)float.Parse(entry.Value.TimeLeft);

            Debug.Log("here");
            if (entry.Value.Name.Equals(GameManager.playerUsername))
            {
                userWPMText.text = "Your Est: WPM: " + wpm;
                userAccText.text = "Your Accuracy: " + accuracy + "%";
                player1WPMText.text = "1. " + entry.Value.Name + " WPM: " + wpm;
                player1AccText.text = "1. " + entry.Value.Name + " Accuracy: " + accuracy + "%";
            }
            else
            {
                player2AccText.text = "2. " + entry.Value.Name + " Accuracy: " + accuracy + "%";
                player2WPMText.text = "2. " + entry.Value.Name + " WPM: " + wpm;
            }
        }
    }

    public void highlightWords()
    {
        string newWords = "";
        string typedWords = userInput.text;

        for (int i = 0; i < typedWords.Length; i++)
        {
            if (typedWords[i] == cleanWords[i])
            {
                newWords += "<mark=#00FF0032>" + cleanWords[i] + "</mark>";
            }
            else
            {
                if (char.IsWhiteSpace(cleanWords[i]))
                {
                    newWords += "<mark=#FF000032>" + cleanWords[i] + "</mark>";
                }
                else
                {
                    newWords += "<mark=#FF000032>" + cleanWords[i] + "</mark>";
                }
            }
        }

        newWords += cleanWords.Substring(typedWords.Length);
        words.text = newWords;
    }
}
