

import { GoogleGenAI, GenerateContentResponse } from "@google/genai";

// Always initialize with the direct environment variable as per guidelines
const getAIClient = () => {
  return new GoogleGenAI({ apiKey: process.env.API_KEY });
};

export const editImageWithGemini = async (
  base64Image: string,
  prompt: string,
  mimeType: string = 'image/png'
): Promise<string | null> => {
  try {
    const ai = getAIClient();
    const cleanBase64 = base64Image.split(',')[1] || base64Image;

    const response: GenerateContentResponse = await ai.models.generateContent({
      model: 'gemini-2.5-flash-image',
      contents: {
        parts: [
          {
            inlineData: {
              data: cleanBase64,
              mimeType: mimeType,
            },
          },
          {
            text: prompt,
          },
        ],
      },
    });

    // Extract the generated image from the response parts (do not assume position)
    for (const part of response.candidates?.[0]?.content?.parts || []) {
      if (part.inlineData) {
        return `data:${mimeType};base64,${part.inlineData.data}`;
      }
    }
    return null;
  } catch (error) {
    console.error("Error editing image:", error);
    throw error;
  }
};

export const generateLogoWithGemini = async (brandPrompt: string): Promise<string | null> => {
  try {
    const ai = getAIClient();
    const systemPrompt = `Create a high-end, professional branding asset for "EduFlow". 
    Focus on a fluid, organic aesthetic with elegant blue and purple gradients. 
    The typography should combine a clean modern sans-serif for "Edu" and a sophisticated, flowing artistic script for "Flow". 
    Incorporate abstract fluid shapes, silk-like waves, or glowing light trails to represent the 'flow' of knowledge. 
    Style: Premium, minimalist but artistic, tech-forward. ${brandPrompt}`;
    
    const response: GenerateContentResponse = await ai.models.generateContent({
      model: 'gemini-2.5-flash-image',
      contents: {
        parts: [{ text: systemPrompt }],
      },
      config: {
        imageConfig: {
          aspectRatio: "1:1"
        }
      }
    });

    // Extract image data from parts as recommended for the nano banana series models
    for (const part of response.candidates?.[0]?.content?.parts || []) {
      if (part.inlineData) {
        return `data:image/png;base64,${part.inlineData.data}`;
      }
    }
    return null;
  } catch (error) {
    console.error("Error generating logo:", error);
    throw error;
  }
};