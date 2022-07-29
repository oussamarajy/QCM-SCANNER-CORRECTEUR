import time
import urllib

import flask
import werkzeug
from flask import Flask, render_template, Response, send_from_directory, jsonify, request
import cv2
import numpy as np
from imutils.perspective import four_point_transform
import base64
import io
import utilis
from imutils import contours as imcont
import requests
from PIL import Image

app = Flask(__name__, static_url_path='/static')

face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')


# camera = cv2.VideoCapture(0)
@app.route("/static/<path:path>")
def static_dir(path):
    return send_from_directory("static", path)


@app.route('/v')
def index():
    return render_template('index.html')


# @app.route('/video_feed')
# def video_feed():
#     return Response(gen_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

def get_as_base64(url):
    return base64.b64encode(requests.get(url).content)


# def gen_frames():
#     while True:
#         success, frame = camera.read()  # read the camera frame
#
#         gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
#
#         # Detect the faces
#         faces = face_cascade.detectMultiScale(gray, 1.1, 4)
#
#         # Draw the rectangle around each face
#         for (x, y, w, h) in faces:
#             cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)
#
#
#         if not success:
#             break
#         else:
#             ret, buffer = cv2.imencode('.jpg', frame)
#             frame = buffer.tobytes()
#             yield (b'--frame\r\n'
#                    b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')  # concat frame one by one and show result
@app.route('/list-answers', methods=['GET', 'POST'])
def list_answers():
    try:
        decoded_data = base64.b64decode(str(request.values.get("image_list")))
        np_data = np.fromstring(decoded_data, np.uint8)
        img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
        # req = urllib.urlopen('http://127.0.0.1:5000/static/qcm.png')
        # arr = np.asarray(bytearray(req.read()), dtype=np.uint8)
        # img = cv2.imdecode(arr, -1)
        # img = cv2.imread('http://127.0.0.1:5000/static/qcm.png')

        widthImg = 720
        heightImg = 1280

        questions = 20
        choices = 5

        answers = [2, 1, 0, 4, 1, 3, 0, 2, 2, 0, 1, 0, 1, 4, 4, 2, 3, 3, 0, 2]
        img = cv2.resize(img, (widthImg, heightImg))
        imgfinal = img.copy()
        imgContours = img.copy()
        imgGray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        imgBlur = cv2.GaussianBlur(imgGray, (5, 5), 0)
        imgCanny = cv2.Canny(imgBlur, 10, 50)

        contours, hierarchy = cv2.findContours(imgCanny, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

        rectCon = utilis.rectContour(contours)
        bggestCont = utilis.getCornerPonts(rectCon[0])
        ResCont = utilis.getCornerPonts(rectCon[1])
        InfoCont = utilis.getCornerPonts(rectCon[2])
        put_res_con = ResCont

        utilis.recorder(bggestCont)
        utilis.recorder(ResCont)
        print(len(contours))

        if bggestCont.size != 0:
            cv2.drawContours(imgContours, bggestCont, -1, (0, 255, 0), 3)
            cv2.drawContours(imgContours, ResCont, -1, (0, 255, 0), 3)

            bggestCont = utilis.recorder(bggestCont)
            ResCont = utilis.recorder(ResCont)

            pt1 = np.float32(bggestCont)
            pt2 = np.float32([[0, 0], [widthImg, 0], [0, heightImg], [widthImg, heightImg]])
            matrx = cv2.getPerspectiveTransform(pt1, pt2)
            imgWarpColor = cv2.warpPerspective(img, matrx, (widthImg, heightImg))

            pt1_res = np.float32(ResCont)
            pt2_res = np.float32([[0, 0], [196, 0], [0, 52], [196, 52]])
            matrx_res = cv2.getPerspectiveTransform(pt1_res, pt2_res)
            imgWarpColor_res = cv2.warpPerspective(img, matrx_res, (196, 52))
            # threshold

            imgwarGray = cv2.cvtColor(imgWarpColor, cv2.COLOR_RGB2GRAY)
            imgThresh = cv2.threshold(imgwarGray, 100, 255, cv2.THRESH_BINARY_INV)[1]
            imgwarGray_res = cv2.cvtColor(imgWarpColor_res, cv2.COLOR_RGB2GRAY)
            imgThresh_res = cv2.threshold(imgwarGray_res, 150, 255, cv2.THRESH_BINARY_INV)[1]

            boxes = utilis.splitBoxes(imgThresh)
            # print(len(boxes))
            # cv2.imshow("test", boxes[0])
            # print(cv2.countNonZero(boxes[3]))
            myPixelVal = np.zeros((questions, choices))  # because we have 5 question
            countC = 0
            countR = 0
            for image in boxes:
                totalPixels = cv2.countNonZero(image)
                myPixelVal[countR][countC] = totalPixels
                countC += 1
                if (countC == choices):
                    countR += 1
                    countC = 0
            # print(myPixelVal)

            myIndex = []
            for x in range(0, questions):
                arr = myPixelVal[x]
                index = np.where(arr == np.amax(arr))
                myIndex.append(index[0][0])

        return str(myIndex)  # jsonify(list=str(myIndex)) #render_template('result.html', output = str(myIndex))
    except Exception as e:
        return "Error Image"


######
@app.route('/', methods=['GET', 'POST'])
def handle_request():
    try:

        answers_list = str(request.values.get("answers"))
        print("before:" + answers_list)
        answers_list = answers_list.replace("[", "")
        answers_list = answers_list.replace("]", "")
        answers_list = answers_list.replace(" ", "")
        answers_list = answers_list.split(",")
        answers = []
        for i in answers_list:
            answers.append(int(i))
        # print("after:"+str(answers))
        # answers =  #[2, 1, 0, 4, 1, 3, 0, 2, 2, 0, 1, 0, 1, 4, 4, 2, 3, 3, 0, 2]
        decoded_data = base64.b64decode(str(request.values.get("image")))
        np_data = np.fromstring(decoded_data, np.uint8)
        img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

        # img_rgb = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
        # img_gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)

        # face_locations = face_recognition.face_locations(img_gray)
        # for(top,right,bottom,left) in face_locations:
        # cv2.rectangle(img_rgb,(left,top),(right,bottom),(0,0,255),8)
        widthImg = 720
        heightImg = 1280

        questions = 20
        choices = 5

        # answers = [2, 1, 0, 4, 1, 3, 0, 2, 2, 0, 1, 0, 1, 4, 4, 2, 3, 3, 0, 2]
        img = cv2.resize(img, (widthImg, heightImg))
        imgfinal = img.copy()
        imgContours = img.copy()
        imgGray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        imgBlur = cv2.GaussianBlur(imgGray, (5, 5), 0)
        imgCanny = cv2.Canny(imgBlur, 10, 50)

        contours, hierarchy = cv2.findContours(imgCanny, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

        rectCon = utilis.rectContour(contours)
        bggestCont = utilis.getCornerPonts(rectCon[0])
        ResCont = utilis.getCornerPonts(rectCon[1])
        InfoCont = utilis.getCornerPonts(rectCon[2])
        put_res_con = ResCont
        utilis.recorder(bggestCont)
        utilis.recorder(ResCont)
        print(len(contours))

        if bggestCont.size != 0:
            cv2.drawContours(imgContours, bggestCont, -1, (0, 255, 0), 3)
            cv2.drawContours(imgContours, ResCont, -1, (0, 255, 0), 3)

            bggestCont = utilis.recorder(bggestCont)
            ResCont = utilis.recorder(ResCont)

            pt1 = np.float32(bggestCont)
            pt2 = np.float32([[0, 0], [widthImg, 0], [0, heightImg], [widthImg, heightImg]])
            matrx = cv2.getPerspectiveTransform(pt1, pt2)
            imgWarpColor = cv2.warpPerspective(img, matrx, (widthImg, heightImg))

            pt1_res = np.float32(ResCont)
            pt2_res = np.float32([[0, 0], [196, 0], [0, 52], [196, 52]])
            matrx_res = cv2.getPerspectiveTransform(pt1_res, pt2_res)
            imgWarpColor_res = cv2.warpPerspective(img, matrx_res, (196, 52))
            # threshold

            imgwarGray = cv2.cvtColor(imgWarpColor, cv2.COLOR_RGB2GRAY)
            imgThresh = cv2.threshold(imgwarGray, 100, 255, cv2.THRESH_BINARY_INV)[1]
            imgwarGray_res = cv2.cvtColor(imgWarpColor_res, cv2.COLOR_RGB2GRAY)
            imgThresh_res = cv2.threshold(imgwarGray_res, 150, 255, cv2.THRESH_BINARY_INV)[1]

            boxes = utilis.splitBoxes(imgThresh)
            # print(len(boxes))
            # cv2.imshow("test", boxes[0])
            # print(cv2.countNonZero(boxes[3]))
            myPixelVal = np.zeros((questions, choices))  # because we have 5 question
            countC = 0
            countR = 0
            for image in boxes:
                totalPixels = cv2.countNonZero(image)
                myPixelVal[countR][countC] = totalPixels
                countC += 1
                if (countC == choices):
                    countR += 1
                    countC = 0
            # print(myPixelVal)

            myIndex = []
            for x in range(0, questions):
                arr = myPixelVal[x]
                index = np.where(arr == np.amax(arr))
                myIndex.append(index[0][0])
            # print("les choix selectionne",myIndex)
            list_res = []
            for x in range(0, questions):
                if answers[x] == myIndex[x]:
                    list_res.append(1)
                else:
                    list_res.append(0)
            # imgRes = imgWarpColor.copy()
            # imgRes = utilis.showAnswers(imgRes, myIndex, list_res, answers, questions, choices)

            # imgDr = np.zeros_like(imgWarpColor)
            # imgDr = utilis.showAnswers(imgDr, myIndex, list_res, answers, questions, choices)
            # Invmatrx = cv2.getPerspectiveTransform(pt2, pt1)
            # imgcc = cv2.warpPerspective(imgDr, Invmatrx, (widthImg, heightImg))
            # imgfinal = cv2.addWeighted(imgfinal,0.5, imgcc,1,0)

        total = sum(list_res)
        # print("tot",total)

        if total == 10:
            color = (255, 0, 0)
        elif total < 10:
            color = (0, 0, 255)
        else:
            color = (0, 255, 0)

        x, y, w, h = cv2.boundingRect(ResCont)
        cv2.putText(imgfinal, str(total) + "/20", (x + 40, y + 70), cv2.FONT_HERSHEY_SIMPLEX, 2, color, 3)
        questionCnts = []

        for i in range(3, 23):
            questionCnts.append(rectCon[i])
            cv2.drawContours(imgContours, rectCon[i], -1, (0, 255, 0), 3)
        questionCnts = imcont.sort_contours(questionCnts, method="top-to-bottom")[0]
        c = 0
        for i in questionCnts:
            if list_res[c] == 1:
                cv2.drawContours(imgfinal, i, -1, (0, 255, 0), 3)
            else:
                cv2.drawContours(imgfinal, i, -1, (0, 0, 255), 3)
                if (answers[c] == 0):
                    rep = "A"
                elif (answers[c] == 1):
                    rep = "B"
                elif (answers[c] == 2):
                    rep = "C"
                elif (answers[c] == 3):
                    rep = "D"
                elif (answers[c] == 4):
                    rep = "E"
                x, y, w, h = cv2.boundingRect(i)
                cv2.putText(imgfinal, "La reponse : " + rep, (x + 60, y + 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5,
                            (255, 0, 0),
                            2)
            peri = cv2.arcLength(i, True)
            approx = cv2.approxPolyDP(i, 0.01 * peri, True)
            x, y = approx[0][0]
            # cv2.putText(imgfinal, str(c), (x,y), cv2.FONT_HERSHEY_SIMPLEX, 1, (255,0,0), 1, cv2.LINE_AA)
            c += 1

        # print(myIndex)

        # imgBlank = np.zeros_like(img)
        # cv2.imshow("img", img)
        # cv2.imshow("blur", imgBlur)
        # cv2.imshow("gray", imgGray)
        # cv2.imshow("edge", imgCanny)
        # cv2.imshow("imgContours", imgContours)
        # cv2.imshow("warp", imgWarpColor)
        # cv2.imshow("THRESH", imgThresh)
        # cv2.imshow("warp_res", imgWarpColor_res)
        # cv2.imwrite("save_test_img.jpg", imgfinal)
        # cv2.imshow("THRESH_RES", imgThresh_res)
        # cv2.imshow("resultat", imgRes)
        # cv2.imshow("resultat", imgfinal)
        """
    epsilon = 0.01 * cv2.arcLength(InfoCont, True)
    approx = cv2.approxPolyDP(InfoCont, epsilon, True)
    result_img = four_point_transform(imgfinal, approx.reshape(4, 2))
    gray = cv2.cvtColor(result_img, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (3,3), 0)

    thresh = cv2.threshold(blur, 220, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

    # Morph open to remove noise and invert image
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5,5))
    opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations=1)
    invert = 255 - opening
    #cv2.imshow("info", thresh)
    text = "gfgf"#pytesseract.image_to_string(thresh, config=tessdata_dir_config)
    #print(text)

    #excel
    book = openpyxl.load_workbook('file.xlsx')

    sheet = book.active

    cells = sheet['A1': 'B6']
    c = 1
    max_ratio = 0
    for c1, c2 in cells:
        if(c1.value == None and c2.value == None):
            break


        text_excel = str(c1.value)+" "+str(c2.value)
        taux = utilis.similar(text_excel, text)
        taux = taux*100

        if(taux > max_ratio):
            max_ratio = taux
            index = c
        c += 1
    #print("taux ori", max_ratio)
    if(max_ratio>=30):
       sheet.cell(row=index, column=3).value = total
    book.save('resultat.xlsx')
    cv2.waitKey(0)
    """
        color_coverted = cv2.cvtColor(imgfinal, cv2.COLOR_BGR2RGB)

        pil_im = Image.fromarray(color_coverted)
        buff = io.BytesIO()
        pil_im.save(buff, format="PNG")
        img_str = base64.b64encode(buff.getvalue())
        return "" + str(img_str, 'utf-8')
        # return render_template('result.html', output=str(img_str,'utf-8'))
        # return ""+str(request.values.get("image"))
    except:
        return str("Error Image")  # jsonify({"image":"Error Image"})


@app.route('/image-name', methods=['GET', 'POST'])
def main():
    try:
        decoded_data = base64.b64decode(str(request.values.get("image_name")))
        np_data = np.fromstring(decoded_data, np.uint8)
        img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

        widthImg = 720
        heightImg = 1280

        img = cv2.resize(img, (widthImg, heightImg))
        imgfinal = img.copy()
        imgContours = img.copy()
        imgGray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        imgBlur = cv2.GaussianBlur(imgGray, (5, 5), 0)
        imgCanny = cv2.Canny(imgBlur, 10, 50)

        contours, hierarchy = cv2.findContours(imgCanny, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

        rectCon = utilis.rectContour(contours)
        bggestCont = utilis.getCornerPonts(rectCon[0])
        ResCont = utilis.getCornerPonts(rectCon[1])
        InfoCont = utilis.getCornerPonts(rectCon[2])
        put_res_con = ResCont
        utilis.recorder(bggestCont)
        utilis.recorder(ResCont)
        print(len(contours))

        epsilon = 0.01 * cv2.arcLength(InfoCont, True)
        approx = cv2.approxPolyDP(InfoCont, epsilon, True)
        result_img = four_point_transform(img, approx.reshape(4, 2))
        """
            gray = cv2.cvtColor(result_img, cv2.COLOR_BGR2GRAY)
            blur = cv2.GaussianBlur(gray, (3, 3), 0)

            thresh = cv2.threshold(blur, 220, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]
            """
        color_coverted = cv2.cvtColor(result_img, cv2.COLOR_BGR2RGB)

        pil_im = Image.fromarray(color_coverted)
        buff = io.BytesIO()
        pil_im.save(buff, format="PNG")
        img_str = base64.b64encode(buff.getvalue())
        return "" + str(img_str, 'utf-8')
    # except:
    #   return "Error Image"
    except:
        return "error"


if __name__ == "__main__":
    app.run(debug=True)
